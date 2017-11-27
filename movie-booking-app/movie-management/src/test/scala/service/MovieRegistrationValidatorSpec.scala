package service

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit, TestProbe}
import com.ayon.movieservice.common.{CompleteResult, Failure, FailureType}
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.{RegisterNewMovie, ValidatedRequest, ValidationRequest}
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager.{FindMovieTitle, MovieFoundWithTitle, wrongImdbId}
import com.ayon.movieservice.movieregistration.service.MovieRegistrationValidator
import com.ayon.movieservice.movieregistration.service.MovieRegistrationValidator._
import com.typesafe.config.ConfigFactory
import org.scalatest._

import scala.concurrent.duration._

/**
  * Created by AYON SANYAL on 19-11-2017.
  */
class MovieRegistrationValidatorSpec
  extends TestKit(ActorSystem("MovieRegistrationValidator"))
    with ImplicitSender
    with WordSpecLike
    with Matchers with BeforeAndAfterAll
{

  import utils.TestData._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
   val movieRegistrationValidatorActorProbe =TestProbe()
  val movieRegistrationValidator = TestFSMRef(new MovieRegistrationValidator{
    override def movieRegistrationValidationManagerActor: ActorRef = movieRegistrationValidatorActorProbe.ref
  })




  "MovieRegistrationValidator" should{

    "Start with WaitingForData as State and NoData as Data and should switch to CollectingMovieTitle state when it receives ValidationRequest message " in {
      val movieRegistrationValidator = TestFSMRef(new MovieRegistrationValidator())
      movieRegistrationValidator.stateName should  equal(WaitingForData)
      movieRegistrationValidator.stateData should  equal(NoData)
      val request = ValidationRequest(RegisterNewMovie(movieInfo1))
      movieRegistrationValidator ! request

      movieRegistrationValidator.stateName should  equal(CollectingMovieTitle)
      movieRegistrationValidator.stateData should equal(GatherTitleForTheMovie(Inputs(self, request)))

    }

     "Transform into PreparingValidatedRequest state from CollectingMovieTitle state when it sends FindMovieTitle request to movieRegistrationValidationManagerActor " in {

       val request = ValidationRequest(RegisterNewMovie(movieInfo1))
       movieRegistrationValidator.setState(CollectingMovieTitle,GatherTitleForTheMovie(Inputs(self, request)))
       val movieURL = ConfigFactory.load.getString("myApiFilmsUrl")
       val token = ConfigFactory.load.getString("myApiFilmsToken")
       val url = s"$movieURL${request.registerNewMovie.information.imdbId}&token=$token"

       movieRegistrationValidatorActorProbe.expectMsg(60 seconds,FindMovieTitle(url))
       movieRegistrationValidator.stateName should  equal(PreparingValidatedRequest)
       movieRegistrationValidator.stateData should equal(GatheringResults(Inputs(self, request),null))
    }



    "When in PreparingValidatedRequest state ,then on receiving the Failure event should respond with the same and stop" in
      {
        val request = ValidationRequest(RegisterNewMovie(movieInfo1))
        movieRegistrationValidator.setState(PreparingValidatedRequest,GatheringResults(Inputs(self, request),null))
        movieRegistrationValidator ! Failure(FailureType.Validation,wrongImdbId)
        expectMsg(60 seconds,Failure(FailureType.Validation,wrongImdbId))
      }

  }





}
