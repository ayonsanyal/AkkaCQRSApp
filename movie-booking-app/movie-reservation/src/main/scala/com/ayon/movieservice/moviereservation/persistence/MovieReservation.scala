package com.ayon.movieservice.moviereservation.persistence

import akka.actor.Props
import com.ayon.movieservice.common.{DatamodelReader, EntityEvent, EntityFieldsObject, MovieServicePersistentEntity}
import com.ayon.movieservice.moviereservation.MovieReservationDatamodel



/**
  * Created by AYON SANYAL on 21-11-2017
  */


  object MovieReservationInformationFO{
    def empty = MovieReservationInformationFO("","","","",0,0)
  }

  case class MovieReservationInformationFO(id:String,imdbId:String,screenId:String,movieTitle:String, availableSeats:Int,reservedSeats:Int,deleted:Boolean = false) extends EntityFieldsObject[String,MovieReservationInformationFO] {
    def assignId(id:String) = this.copy(id = id)
    def markDeleted = this.copy(deleted = true)

  }


/**
  * A persistence actor for movie reservation actor responsible for command persistence into journal which is cassandra
  * in our case.
  * @param id
  */
class MovieReservation(id:String) extends MovieServicePersistentEntity[MovieReservationInformationFO](id) {
  import MovieReservation._
  import Command._
  import Event._

  override def initialState= MovieReservationInformationFO.empty
  override def snapshotAfterCount = Some(50)

  def additionalCommandHandling:Receive = {
    case ReserveMovie(movie) =>
      persist(MovieReserved(movie))(handleEventAndRespond())
    case ReserveSeat(seat) => persist((SeatReserved(seat)))(handleEventAndRespond())
  }

  def isCreateMessage(cmd:Any) = cmd match{
    case ReserveMovie => true
    case ReserveSeat=> true
    case _ => true
  }

  def handleEvent(event:EntityEvent):Unit = event match {
    case MovieReserved(movie) =>
      state = movie
    case SeatReserved(seat)=>
      state=state.copy(reservedSeats = seat)
  }


}

object MovieReservation{

  val EntityType = "moviereservation"

  /**
    * Command for movie reservation module which will be persisted .C from CQRS pattern.
    */
  object Command{

    case class ReserveMovie(reservationDetails:MovieReservationInformationFO)

    case class ReserveSeat(seatCount:Int)
  }

  object Event{
    trait MovieReservationEvent extends EntityEvent{def entityType = EntityType}
    case class MovieReserved(movieReservationFo:MovieReservationInformationFO) extends MovieReservationEvent{

      override def toDatamodel={
        val  movieReservationdataModel = MovieReservationDatamodel.MovieReservation.newBuilder().
          setId(movieReservationFo.id).
          setImdbId(movieReservationFo.imdbId).
          setScreenId(movieReservationFo.screenId).
          setMovieTitle(movieReservationFo.movieTitle).
          setAvailableSeats(movieReservationFo.availableSeats).
          setReservedSeats(movieReservationFo.reservedSeats).
          build

        MovieReservationDatamodel.MovieReserved.newBuilder().
          setMoviereservation(movieReservationdataModel).
          build
      }


    }

    /**
      * Event to be used by read model
      */
    object MovieReserved extends DatamodelReader{

      def fromDatamodel ={
        case movieReserved:MovieReservationDatamodel.MovieReserved =>
          val movieReservationdataModel = movieReserved.getMoviereservation()
          val movieReservationInformation = MovieReservationInformationFO(movieReservationdataModel.getId,movieReservationdataModel.getImdbId,movieReservationdataModel.getScreenId,
            movieReservationdataModel.getMovieTitle,movieReservationdataModel.getAvailableSeats,movieReservationdataModel.getReservedSeats)
          MovieReserved(movieReservationInformation)
      }
    }
    case class SeatReserved(seatCount:Int) extends  MovieReservationEvent{

      override def toDatamodel ={
             MovieReservationDatamodel.SeatReserved.newBuilder().setSeatCount(seatCount).build

      }
    }

    object SeatReserved extends DatamodelReader{
      override def fromDatamodel ={
        case seatReserved: MovieReservationDatamodel.SeatReserved=>
          val seatsBooked = seatReserved.getSeatCount
          SeatReserved(seatsBooked)
      }
    }
  }
 def props(id:String) = Props(classOf[MovieReservation],id)
}
