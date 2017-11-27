package com.ayon.movieservice.moviereservation.service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import com.ayon.movieservice.common._
import com.ayon.movieservice.common.MovieServicePersistentEntity.GetState
import com.ayon.movieservice.movieregistration.service.MovieInformationManager
import com.ayon.movieservice.moviereservation.persistence.MovieReservation.Command.ReserveSeat
import com.ayon.movieservice.moviereservation.persistence.{MovieReservation, MovieReservationInformationFO}
import com.ayon.movieservice.moviereservation.service.MovieReservationManager.{AllSeatReservedForMovie, ReserveMovieSeat, ValidatedReservationRequest}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.ayon.movieservice.moviereservation.utils.MovieReservationTestData._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Success

/**
  * Created by AYON SANYAL on 22-11-2017
  */
class MovieReservationManagerSpec extends TestKit(ActorSystem("MovieInformationManagerSpec"))  with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  class scoping  extends  TestKit(system) with ImplicitSender
  {
    val movieInfoManager = TestProbe(MovieInformationManager.Name)
    val movieEntityProbe= TestProbe()
    val movieValidatator= TestProbe()
    val ResolveTimeout = 120 seconds
    val nameMap =
      Map(
        MovieInformationManager.Name -> movieInfoManager.ref.path.name

      )
    val movieInfoManagerMock = TestActorRef(new MovieReservationManager{
      override def lookupOrCreateChild(id:String) = movieEntityProbe.ref
      override def lookup(name:String) =
        context.actorSelection(s"akka://default/system/${nameMap.getOrElse(name, "")}")
      override  def stateValidationResult(id:String) = {
        import akka.pattern.ask

        import system.dispatcher

        implicit val timeout = Timeout(10 seconds)
        val result= CompleteResult(movieReservationInfoFO1)
        (movieEntityProbe.ref ? GetState).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieEntityProbe.expectMsg(0 seconds,GetState)
        movieEntityProbe.reply(result)
        Future(result)
      }
      override def reservationValidationManager=movieValidatator.ref
    })

    val movieReservationManagerMockFailureCase = TestActorRef(new MovieReservationManager{
      override def lookupOrCreateChild(id:String) = movieEntityProbe.ref
      override def lookup(name:String) =
        context.actorSelection(s"akka://default/system/${nameMap.getOrElse(name, "")}")
      override  def stateValidationResult(id:String) = {
        import akka.pattern.ask

        import system.dispatcher

        implicit val timeout = Timeout(10 seconds)
        val result= CompleteResult(movieReservationInfoFO2)
        (movieEntityProbe.ref ? GetState).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieEntityProbe.expectMsg(0 seconds,GetState)
        movieEntityProbe.reply(result)
        Future(result)
      }
      override def reservationValidationManager=movieValidatator.ref
    })

    val movieInfoManagerMockEmptyResultCase = TestActorRef(new MovieReservationManager
    {
      override def lookupOrCreateChild(id:String) = movieEntityProbe.ref
      override def lookup(name:String) =
        context.actorSelection(s"akka://default/system/${nameMap.getOrElse(name, "")}")
      override  def stateValidationResult(id:String) = {
        import akka.pattern.ask

        import system.dispatcher

        implicit val timeout = Timeout(10 seconds)
        val result= EmptyResult
        (movieEntityProbe.ref ? GetState).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieEntityProbe.expectMsg(0 seconds,GetState)
        movieEntityProbe.reply(result)
        Future(result)
      }
      override def reservationValidationManager=movieValidatator.ref
    })

  }


  "MovieReservationManager" should {
    "Forward the seat booking request to Persistent Entity when ValidatedRequest message is received" in new scoping{
      movieInfoManagerMock ! ValidatedReservationRequest(movieInfoFO1)
      movieEntityProbe.expectMsg(ResolveTimeout,MovieReservation.Command.ReserveMovie(movieReservationInfoFO1))
    }
    "Forward the ReserveSeat request i.e(just update the seat number for a movie where first seat has been already booked)to Persistent Entity when  " in new scoping{
      movieInfoManagerMock ! ReserveMovieSeat(moviereservationInfo1)
      movieEntityProbe.expectMsg(ResolveTimeout,ReserveSeat(movieReservationInfoFO1.reservedSeats+1))

    }

    "Reply with Failure Message for the condition when all seats have been taken" in new scoping
    {
      movieReservationManagerMockFailureCase ! ReserveMovieSeat(moviereservationInfo1)
      val result= Failure(FailureType.Validation, AllSeatReservedForMovie)
      expectMsg(ResolveTimeout,result)
    }

    "Forward the ReservationRequest to ValidationManager when it is a fresh booking request" in new scoping{
      movieInfoManagerMockEmptyResultCase ! ReserveMovieSeat(moviereservationInfo1)
      movieValidatator.expectMsg(ResolveTimeout,ReserveMovieSeat(moviereservationInfo1))
    }


  }

}
