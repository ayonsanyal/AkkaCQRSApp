package service

import akka.actor.{ActorSystem, Props}

import scala.concurrent.duration._
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.ayon.movieservice.common.{CompleteResult, Failure, FailureType}
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager.{FindMovieTitle, MovieFoundWithTitle, wrongImdbId}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import utils.TestData._

/**
  * Created by AYON SANYAL on 19-11-2017
  */
class MovieInformationValidationManagerSpec extends TestKit(ActorSystem("MovieTitleValidatorSpec"))  with WordSpecLike
  with Matchers with BeforeAndAfterAll
  with ImplicitSender {




  val movieURL = ConfigFactory.load.getString("myApiFilmsUrl")

  val token = ConfigFactory.load.getString("myApiFilmsToken")
  val url = s"$movieURL${movieInfo1.imdbId}&token=$token"
  val urlWithWrondImdbId = s"$movieURL${movieInfoWrong.imdbId}&token=$token"
  val ResolveTimeout = 120 seconds
  val movieTitleValidatorMockParent= TestProbe()
  val movieTitleValidatorMockChild = movieTitleValidatorMockParent.childActorOf(Props(classOf[MovieInformationValidationManager],movieTitleValidatorMockParent.ref))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "MovieInformationValidationManager" should{

    "Bring the  movie title from myapifilms (i.e 3rd party service) for valid url and imdbId" in {

      movieTitleValidatorMockParent.send(movieTitleValidatorMockChild,FindMovieTitle(url))
      val movieTitleExpected =  CompleteResult(MovieFoundWithTitle(movieInfoFO1.movieTitle))
      movieTitleValidatorMockParent.expectMsg(ResolveTimeout,movieTitleExpected)
    }

    "Return Failure Message for wrong imdbId from myapifilms (i.e 3rd party service)" in {

      val responseExpected = Failure(FailureType.Validation,wrongImdbId)
      movieTitleValidatorMockParent.send(movieTitleValidatorMockChild,FindMovieTitle(urlWithWrondImdbId))
      movieTitleValidatorMockParent.expectMsg(ResolveTimeout,responseExpected)

    }




  }

}
