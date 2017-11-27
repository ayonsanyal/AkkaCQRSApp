package com.ayon.movieservice.moviereservation.persistence

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.ayon.movieservice.common.CompleteResult
import com.ayon.movieservice.common.MovieServicePersistentEntity.GetState
import com.ayon.movieservice.movieregistration.persistence.Movie
import com.ayon.movieservice.moviereservation.persistence.MovieReservation.Command.{ReserveMovie, ReserveSeat}
import com.ayon.movieservice.moviereservation.utils.MovieReservationTestData
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

/**
  * Created by AYON SANYAL on 21-11-2017
  */
class MovieReservationSpec extends TestKit(ActorSystem("MovieReservationSpec")) with WordSpecLike
with Matchers
with BeforeAndAfterAll
with ImplicitSender {
  val ResolveTimeout = 120 seconds
  val movieToReserve = MovieReservationTestData.movieReservationInfoFO1
  val movieReservationActor = system.actorOf(MovieReservation.props(movieToReserve.id))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "MovieReservation" should {

    "Persist on request to reserve a seat or create a new booking for the given movie information" in {
      val movieToReserve = MovieReservationTestData.movieReservationInfoFO1

      movieReservationActor.tell(ReserveMovie(movieToReserve), self)
      expectMsg(ResolveTimeout, CompleteResult(movieToReserve))

    }


    "Update reserved seats when another booking request comes for same movie" in {
      val movieToReserve = MovieReservationTestData.movieReservationInfoFO1
      val seatCount = movieToReserve.reservedSeats + 1
      movieReservationActor ! ReserveSeat(movieToReserve.reservedSeats + 1)
      expectMsg(ResolveTimeout, CompleteResult(movieToReserve.copy(reservedSeats = seatCount)))
      movieReservationActor ! GetState
      expectMsg(ResolveTimeout, CompleteResult(movieToReserve.copy(reservedSeats = seatCount)))

    }
  }



}
