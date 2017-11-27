package service

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.ayon.movieservice.common.CompleteResult
import com.ayon.movieservice.common.MovieServicePersistentEntity.GetState
import com.ayon.movieservice.movieregistration.persistence.Movie.Command.RegisterMovie

import scala.concurrent.duration._
import com.ayon.movieservice.movieregistration.service.MovieInformationManager
import com.ayon.movieservice.movieregistration.service.MovieInformationManager._
import org.scalatest._

/**
  * Created by AYON SANYAL on 18-11-2017
  */
class MovieInformationManagerSpec extends TestKit(ActorSystem("MovieInformationManagerSpec"))  with WordSpecLike
  with Matchers with BeforeAndAfterAll {
  class scoping  extends  TestKit(system) with ImplicitSender
  {
    val movieInfoManager = TestProbe(MovieInformationManager.Name)
    val movieEntityProbe= TestProbe()
    val movieValidatator= TestProbe()
    val nameMap =
      Map(
        MovieInformationManager.Name -> movieInfoManager.ref.path.name

      )
    val movieInfoManagerMock = TestActorRef(new MovieInformationManager{
      override def lookupOrCreateChild(id:String) = movieEntityProbe.ref
      override def lookup(name:String) =
        context.actorSelection(s"akka://default/system/${nameMap.getOrElse(name, "")}")
      override def validator = movieValidatator.ref
    })


  }

  import utils.TestData._
  import scala.concurrent.duration._





  val ResolveTimeout = 120 seconds






  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "MovieInformationManager" should{

   "Forward the request to MovieRegistrationValidator  when the message of type RegisterNewMovie is received" in new scoping{
      movieInfoManagerMock ! RegisterNewMovie(movieInfo1)
      movieValidatator.expectMsg(ResolveTimeout,ValidationRequest(RegisterNewMovie(movieInfo1)))
    }

   "Forward the request to Persistent Entity when ValidatedRequest message is recieved" in new scoping{
      movieInfoManagerMock ! ValidatedRequest(movieInfo1,movieInfoFO1.movieTitle)
      movieEntityProbe.expectMsg(RegisterMovie(movieInfoFO1))
    }

    "Forward the request to Persistent Entity when Find the movie by id request will come" in new scoping{
      movieInfoManagerMock ! FindMovieById(movieInfoFO1.id)
      movieEntityProbe.expectMsg(GetState)
    }

  }

}
