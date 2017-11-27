package routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.ayon.movieservice.common._
import com.ayon.movieservice.movieregistration.JsonProtocol.MoviesInformationJsonProtocol
import com.ayon.movieservice.movieregistration.route.MovieManagementRoutes
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.{MovieInformation, RegisterNewMovie}
import com.ayon.movieservice.movieregistration.service.MovieInformationManager
import org.scalatest.{Matchers, WordSpec, WordSpecLike}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.ayon.movieservice.movieregistration.persistence.{Movie, MovieInformationFO}
import com.ayon.movieservice.movieregistration.persistence.Movie.Command.RegisterMovie
import com.ayon.movieservice.movieregistration.persistence.Movie.movieAlreadyRegistered
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager.wrongImdbId
import utils.TestData.movieInfo1
import utils.{TestData, TestDataUtils}
import spray.json._

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.concurrent.duration._



/**
  * Created by AYON SANYAL on 25-11-2017
  */
class MovieManagementRoutesSpec extends   WordSpecLike with ScalatestRouteTest with Matchers   with MoviesInformationJsonProtocol{

  /**
    *
    */
  class scoping extends  TestKit(system) with ImplicitSender
  {

    val movieInfoManager = TestProbe(MovieInformationManager.Name)
    val mockMovieManagementRouteSuccess = new MovieManagementRoutes(movieInfoManager.ref){


      override def serviceAndComplete[T:ClassTag](msg:Any, ref:ActorRef)(implicit format:JsonFormat[T]):Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut=(movieInfoManager.ref  ? msg).mapTo[ServiceResult[MovieInformationFO]]
        movieInfoManager.expectMsg(0 seconds,msg)
        val result=CompleteResult(movieInfoFo)
        movieInfoManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))
      val  responseFuture= Future.successful(result)
        val resp = MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue), Some(movieInfoFo))
        complete(resp)

      }
    }

    val mockMovieManagementRouteValidationFailure1= new MovieManagementRoutes(movieInfoManager.ref){


      override def serviceAndComplete[T:ClassTag](msg:Any, ref:ActorRef)(implicit format:JsonFormat[T]):Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut=(movieInfoManager.ref  ? msg).mapTo[ServiceResult[MovieInformationFO]]
        movieInfoManager.expectMsg(0 seconds,msg)
        val result=Failure(FailureType.Validation,wrongImdbId)
        movieInfoManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))
        val  responseFuture= Future.successful(result)
        val resp = MovieServiceApiResponse[String](MovieServiceApiResponseMetaData(BadRequest.intValue, Some(result.message)))

        complete((BadRequest,resp))

      }
    }

    val mockMovieManagementRouteValidationFailure2 = new MovieManagementRoutes(movieInfoManager.ref){


      override def serviceAndComplete[T:ClassTag](msg:Any, ref:ActorRef)(implicit format:JsonFormat[T]):Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut=(movieInfoManager.ref  ? msg).mapTo[ServiceResult[MovieInformationFO]]
        movieInfoManager.expectMsg(0 seconds,msg)
        val result=Failure(FailureType.Validation, movieAlreadyRegistered)
        movieInfoManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))
        val  responseFuture= Future.successful(result)
        val resp = MovieServiceApiResponse[String](MovieServiceApiResponseMetaData(BadRequest.intValue, Some(result.message)))

        complete((BadRequest,resp))

      }
    }

  }




  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }





  implicit val movieInfo= TestData.movieInfo1
  val url = TestDataUtils.generalUrl
  val movieWrongInfo = TestData.movieInfoWrong
  val movieInfoFo= TestData.movieInfoFO1
  val id = s"${movieInfo.imdbId}${movieInfo.screenId}"





  "movie registration service" should{

    s"respond with HTTP-${OK} for a new movie with screenId" in  new scoping{

     val movieToRegister= HttpEntity(MediaTypes.`application/json`, TestDataUtils.registrationJson(movieInfo1))



        val result=  Post(url,movieToRegister)~>mockMovieManagementRouteSuccess.routes~> runRoute


        check{

        status shouldBe OK
          responseAs[MovieServiceApiResponse[MovieInformationFO]] shouldEqual
          MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue),Some(movieInfoFo))
      }(result)

    }
    s"respond with a failure message if wrong input is given" in  new scoping{
      val movieToRegister= HttpEntity(MediaTypes.`application/json`, TestDataUtils.registrationJsonWrong(movieWrongInfo))

      val result= Post(TestDataUtils.generalUrl,movieToRegister)~>mockMovieManagementRouteValidationFailure1.routes~>runRoute

      check{
        status shouldBe BadRequest

        responseAs[MovieServiceApiResponse[String]] shouldEqual
          MovieServiceApiResponse(MovieServiceApiResponseMetaData(BadRequest.intValue, Some(wrongImdbId)))
      }(result)
    }
    s"respond with error message if movie already exists" in new scoping{
      val movieToRegister= HttpEntity(MediaTypes.`application/json`, TestDataUtils.registrationJson(movieInfo))

      val result= Post(TestDataUtils.generalUrl,movieToRegister)~>mockMovieManagementRouteValidationFailure2.routes~>runRoute

      check{
        status shouldBe  BadRequest
        responseAs[MovieServiceApiResponse[String]] shouldEqual
          MovieServiceApiResponse(MovieServiceApiResponseMetaData(BadRequest.intValue, Some(movieAlreadyRegistered)))
      }(result)

    }
  }




}
