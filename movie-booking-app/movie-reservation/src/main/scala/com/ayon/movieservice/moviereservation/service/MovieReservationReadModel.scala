package com.ayon.movieservice.moviereservation.service

import akka.actor.Props
import akka.persistence.query.EventEnvelope
import akka.stream.ActorMaterializer
import com.ayon.movieservice.common.{ElasticsearchSupport, MovieServiceManager, ReadModelObject, ViewBuilder}
import com.ayon.movieservice.moviereservation.JsonProtocol.MovieReservationJsonProtocol
import com.ayon.movieservice.moviereservation.persistence.MovieReservation
import com.ayon.movieservice.moviereservation.persistence.MovieReservation.Event.{MovieReserved, SeatReserved}


import scala.concurrent.Future

/** Read Model for MovieReservation.
  * It is responsible to have the data  sync with write model which will be received on demand.
  * This actor will handle  everything regarding the read operations for this module.
  * Created by AYON SANYAL on 23-11-2017
  */
trait MovieReservationReadModel {
def indexRoot="moviereservationinfo"
def entityType=MovieReservation.EntityType
}

case class MovieReservationInformationRM(imdbId:String,screenId:String,movieTitle:String,availableSeats:Int,reservedSeats:Int) extends ReadModelObject {
  override def id: String = s"${imdbId}${screenId}"
}
  object MovieReservationViewBuilder{
  val Name = "movie-reservation-view-builder"
  case class MovieReservationRM(id:String,imdbId:String,screenId:String,movieTitle:String,availableSeats:Int,reservedSeats:Int) extends ReadModelObject


  def props = Props[MovieReservationViewBuilder]
}

/**
  * View  buider actor responsible for creating the view by streaming the data from write model.
  */
class MovieReservationViewBuilder extends ViewBuilder[MovieReservationInformationRM] with MovieReservationReadModel with
  MovieReservationJsonProtocol
{
   import ViewBuilder._
   import MovieReservation.Event._

  def projectionId = "movie-reservation-view-builder"
  implicit val rmFormats = movieReservationInfoRM
  def actionFor(id:String,envelope:EventEnvelope) = envelope.event match {

    case MovieReserved(movie)=>
      log.info("Saving a new movie reservation entity into the elasticsearch index: {}", movie)
      val movieReservationRM = MovieReservationInformationRM(movie.imdbId,movie.screenId,movie.movieTitle,movie.availableSeats,movie.reservedSeats)
      InsertAction(s"${movie.imdbId}${movie.screenId}",movieReservationRM)

    case SeatReserved(seat)=>
      log.info("Updating  reserved seats into the elasticsearch index {} and seat count is {}", id,seat)
      UpdateAction(id,List(s"reservedSeats = ${seat}"),Map("reservedSeatsRecent"->seat))
  }
}

object MovieReservationInformationView{
  val Name= "movie-reservation-informationView"
  case class FindMovieReservationByImDBIDAndScreenID(imdbId:String,screenID: String)
  def props = Props[MovieReservationInformationView]
}


/**
  * A view for the movie reservation module responsible for read only operation.
  */
class MovieReservationInformationView extends MovieReservationReadModel with MovieServiceManager with ElasticsearchSupport  with MovieReservationJsonProtocol
{
  import context.dispatcher
  import MovieReservationViewBuilder._
  import MovieReservationInformationView._


  implicit  val materializer = ActorMaterializer()
  implicit val rmFormats = movieReservationInfoRM
  override def receive = {

      case FindMovieReservationByImDBIDAndScreenID(imdbId,screenID)=>
        log.info("request accepted with {} and {}",imdbId,screenID)

        val results = queryElasticsearch[MovieReservationInformationRM](s"imdbId:${imdbId} AND screenId:${screenID}")
        pipeResponse(results)




  }

  def lookup(name: String) = context.actorSelection(s"/user/$name")

}

