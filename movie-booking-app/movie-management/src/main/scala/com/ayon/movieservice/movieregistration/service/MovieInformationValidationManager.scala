package com.ayon.movieservice.movieregistration.service

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.ayon.movieservice.common._
import play.api.libs.json.{JsDefined, Json}
import scala.concurrent.{ExecutionContext, Future}


/**
  * Companion of MovieInformationValidationManager
  * Created by AYON SANYAL on 20-11-2017
  */

object MovieInformationValidationManager {



case class FindMovieTitle(url:String)
  sealed trait MovieTitleResponse
case class MovieFoundWithTitle(movieTitle:String) extends MovieTitleResponse
case object MovieTitleNotFound extends MovieTitleResponse
val wrongImdbId=  ErrorMessage("wrong imdb id", Some("Title for this movie is not found,please try with correct imdbId"))

  def parse(jsonString: String): MovieTitleResponse = {
    val jsValue = Json.parse(jsonString)
    jsValue \ "error" match {
      case JsDefined(error) => MovieTitleNotFound
      case _ => MovieFoundWithTitle(((jsValue \ "data" \ "movies")(0) \ "title").as[String])
    }
  }



}

/**
  * An actor responsible to gather movie title from 3rd party api for the valid imdb ID.
  * @param parent
  */
class MovieInformationValidationManager(parent: ActorRef) extends Actor

  with ActorLogging {
  import MovieInformationValidationManager._
  import akka.pattern.pipe
  import context.dispatcher


  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)

  override def receive ={

    case FindMovieTitle(url)=> {
    val response:Future[HttpResponse]= http.singleRequest(HttpRequest(uri=url))
      val result = response.map{
        case HttpResponse(StatusCodes.OK, _, entity, _)=> {
          val movieTitleFuture = bodyToString(entity).map(parse)
          movieTitleFuture onComplete {
            case util.Success(movieTitleFound:MovieFoundWithTitle)=> parent ! CompleteResult(movieTitleFound)
            case util.Success(MovieTitleNotFound)=> parent ! Failure(FailureType.Validation,wrongImdbId)

          }

        }
        case _ => parent !Failure(FailureType.Service,ServiceResult.UnexpectedFailure)

      }




    }

  }

  def bodyToString(entity: ResponseEntity)(implicit executionContext: ExecutionContext): Future[String] = entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)

}

