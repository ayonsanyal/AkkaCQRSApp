package persistence

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.ayon.movieservice.common.{CompleteResult, Failure, FailureType}

import scala.concurrent.duration._
import com.ayon.movieservice.movieregistration.persistence.Movie
import com.ayon.movieservice.movieregistration.persistence.Movie.Command.RegisterMovie
import com.ayon.movieservice.movieregistration.persistence.Movie.Event.MovieRegistered
import com.ayon.movieservice.movieregistration.persistence.Movie.movieAlreadyRegistered
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.MovieInformation
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import utils.TestData

import scala.util.Success

/**
  * Created by AYON SANYAL
  */
class MovieSpec extends TestKit(ActorSystem("MovieSpec")) with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ImplicitSender {

  val movieToRegister = TestData.movieInfoFO1
  val ResolveTimeout = 120 seconds
  val movieRegistrationActor = system.actorOf(Movie.props(movieToRegister.id))
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "Movie" should{
    "Register a movie with given Information" in {

      movieRegistrationActor.tell(RegisterMovie(movieToRegister),self)
      expectMsg(ResolveTimeout,CompleteResult(movieToRegister))

    }

    "Not allow duplicate entries to persist" in {

      movieRegistrationActor.tell(RegisterMovie(movieToRegister),self)
      val expectedResult= Failure(FailureType.Validation, movieAlreadyRegistered)
      expectMsg(ResolveTimeout,expectedResult)
    }





  }
}
