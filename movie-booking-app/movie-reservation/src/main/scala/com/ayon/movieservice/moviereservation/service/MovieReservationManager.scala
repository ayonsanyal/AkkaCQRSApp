package com.ayon.movieservice.moviereservation.service

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import com.ayon.movieservice.common._
import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.moviereservation.persistence.{MovieReservation, MovieReservationInformationFO}
import com.ayon.movieservice.reservation.service.MovieReservationValidationManager

/**Companion of MovieReservationManager
  * Created by AYON SANYAL on 22-11-2017
  */

object MovieReservationManager {

  val Name = "movie-reservation-manager"

  case class MovieReservationInformation(imdbId: String, screenId: String)

  case class ReserveMovieSeat(information: MovieReservationInformation)

  case class FindMovieById(id: String)

  case class ValidatedReservationRequest(movieregistrationDetails: MovieInformationFO)

  def props = Props[MovieReservationManager]
  def movieReservationValidatorProps = MovieReservationValidationManager.props

  val AllSeatReservedForMovie = ErrorMessage("All seats reserved", Some("All seats are booked"))
}

/**
  * Service actor for movie reservation module i.e responsible for executing business logic ,validation of data and reserve the movie.
  * This actor will handle the write side operation for this module .
  * This actor will consult its child actors for validation and processing and then will delegate the response to route actor.
  *
  */

class MovieReservationManager extends Aggregate[MovieReservationInformationFO, MovieReservation] {


  import com.ayon.movieservice.common.MovieServicePersistentEntity._

  import akka.pattern.ask
  import context.dispatcher
  import concurrent.duration._
  import MovieReservationManager._
  implicit val timeout = Timeout(10 seconds)
  def entityProps(id: String) = MovieReservation.props(id)
  def entity(id:String)=lookupOrCreateChild(id)
  def stateValidationResult(id:String) ={
    val movieReservationEnity = entity(id)
    (movieReservationEnity ? GetState).mapTo[ServiceResult[MovieReservationInformationFO]]
  }
  override def receive = {

    case reserveMovieSeat: ReserveMovieSeat =>
      val id: String = s"${reserveMovieSeat.information.imdbId}${reserveMovieSeat.information.screenId}"

      val caller = sender()


      stateValidationResult(id) onComplete {
        //Update Request
        case util.Success(CompleteResult(movieReserervationFO:MovieReservationInformationFO)) if (movieReserervationFO.reservedSeats < movieReserervationFO.availableSeats) => {
           val updateSeatForTheMovie = MovieReservation.Command.ReserveSeat(movieReserervationFO.reservedSeats + 1)
          lookupOrCreateChild(id).tell(updateSeatForTheMovie,caller)
        }

        case util.Success(CompleteResult(movieReserervationFO:MovieReservationInformationFO)) =>
          caller ! Failure(FailureType.Validation, AllSeatReservedForMovie)
        //Create Request
        case util.Success(EmptyResult) => {

          reservationValidationManager.tell(reserveMovieSeat,caller)
        }
      }

    case ValidatedReservationRequest(movieregistrationDetails) => {
      val movieReserveFo = MovieReservationInformationFO(movieregistrationDetails.id, movieregistrationDetails.imdbId, movieregistrationDetails.screenId, movieregistrationDetails.movieTitle, movieregistrationDetails.availableSeats, 1)
      val bookingReq = MovieReservation.Command.ReserveMovie(movieReserveFo)
      entity(movieregistrationDetails.id).tell(bookingReq,sender())
    }


  }


  def lookup(name: String) = context.actorSelection(s"/user/$name")
  def reservationValidationManager = context.actorOf(movieReservationValidatorProps)
}
