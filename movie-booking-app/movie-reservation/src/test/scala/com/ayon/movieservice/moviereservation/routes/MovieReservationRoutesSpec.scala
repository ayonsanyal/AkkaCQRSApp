package com.ayon.movieservice.moviereservation.routes
import akka.actor.ActorRef
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, OK}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.ayon.movieservice.common._
import com.ayon.movieservice.moviereservation.JsonProtocol.MovieReservationJsonProtocol
import com.ayon.movieservice.moviereservation.persistence.{MovieReservation, MovieReservationInformationFO}
import com.ayon.movieservice.moviereservation.service.{MovieReservationInformationRM, MovieReservationInformationView, MovieReservationManager}
import com.ayon.movieservice.moviereservation.utils.{MovieReservationTestData, MovieReservationtestDataUtils}
import com.ayon.movieservice.reservation.service.MovieReservationValidationManager
import org.scalatest.{Matchers, WordSpecLike}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.reflect.ClassTag

/**
  * Created by AYON SANYAL on 25-11-2017
  */

class MovieReservationRoutesSpec extends  WordSpecLike with ScalatestRouteTest with Matchers with MovieReservationJsonProtocol {


  class scoping extends TestKit(system) with ImplicitSender {

    val movieReservationManager = TestProbe(MovieReservationManager.Name)
    val movieReservationView = TestProbe(MovieReservationInformationView.Name)
    val mockMovieManagementRouteSuccess = new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref) {


      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationManager.ref ? msg).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieReservationManager.expectMsg(0 seconds, msg)

        val result = CompleteResult(moviereservationFo2.copy(reservedSeats = moviereservationFo2.reservedSeats + 1))
        movieReservationManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue), Some(moviereservationFo))
        complete(resp)

      }
    }

    val mockReservationRouteUpdateCase = new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref) {


      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationManager.ref ? msg).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieReservationManager.expectMsg(0 seconds, msg)
        val updatedMovieReservationFO=moviereservationFo.copy(reservedSeats = moviereservationFo.reservedSeats + 1)
        val result = CompleteResult(updatedMovieReservationFO)
        movieReservationManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse[MovieReservationInformationFO](MovieServiceApiResponseMetaData(OK.intValue), Some(updatedMovieReservationFO))

        complete((OK, resp))

      }
    }

    val mockMovieResrvationRouteValidationFailure1 = new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref) {


      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationManager.ref ? msg).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieReservationManager.expectMsg(0 seconds, msg)
        val result = Failure(FailureType.Validation, MovieReservationManager.AllSeatReservedForMovie)
        movieReservationManager.reply(result)
        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse[String](MovieServiceApiResponseMetaData(BadRequest.intValue, Some(result.message)))

        complete((BadRequest, resp))

      }
    }

    val mockMovieReservationRouteValidationFailure2 = new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref) {
      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationManager.ref ? msg).mapTo[ServiceResult[MovieReservationInformationFO]]
        movieReservationManager.expectMsg(0 seconds, msg)
        val result = Failure(FailureType.Validation, MovieReservationValidationManager.MovieNotRegistered)
        movieReservationManager.reply(result)

        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse[String](MovieServiceApiResponseMetaData(BadRequest.intValue, Some(result.message)))

        complete((BadRequest, resp))
      }

    }
    val movieReservationInformationRetrieval = new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref){
      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationView.ref ? msg).mapTo[ServiceResult[MovieReservationInformationRM]]
        movieReservationView.expectMsg(0 seconds, msg)
        val movieRetrievalInformation=List(movieReservationRM)
        val result = CompleteResult(movieRetrievalInformation)
        movieReservationView.reply(result)

        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse[List[MovieReservationInformationRM]](MovieServiceApiResponseMetaData(OK.intValue), Some(movieRetrievalInformation))

        complete((OK, resp))
      }

    }

    val movieReservationInfoRertrievalNoResponse= new MovieReservationRoutes(movieReservationManager.ref, movieReservationView.ref){
      override def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)(implicit format: JsonFormat[T]): Route = {
        import akka.pattern.ask
        import akka.http.scaladsl.server.Directives._
        val fut = (movieReservationView.ref ? msg).mapTo[ServiceResult[MovieReservationInformationRM]]
        movieReservationView.expectMsg(0 seconds, msg)
        val movieRetrievalInformation=List[MovieReservationInformationRM]()
        val result = CompleteResult(movieRetrievalInformation)
        movieReservationView.reply(result)

        assert(fut.isCompleted && fut.value == Some(util.Success(result)))

        val resp = MovieServiceApiResponse[List[MovieReservationInformationRM]](MovieServiceApiResponseMetaData(OK.intValue), Some(movieRetrievalInformation))

        complete((OK, resp))
      }

    }

  }

    implicit val movieInfo = MovieReservationTestData.moviereservationInfo1

    val moviereservationFo = MovieReservationTestData.movieReservationInfoFO1
    val moviereservationFo2 = MovieReservationTestData.movieReservationInfoFO3
    val movieReservationRM = MovieReservationTestData.movieReservationInfoRM
    val movieInfoWrong=MovieReservationTestData.movieReservationInfoWrong
    val url = MovieReservationtestDataUtils.generalUrl


    "movie reservation service " should {
      s"respond with HTTP-${OK} to create a new booking request for imdbId and screenId " in new scoping {


        val movieToReserve = HttpEntity(MediaTypes.`application/json`, MovieReservationtestDataUtils.reservationJson(movieInfo))

        val result = Post(url, movieToReserve) ~> mockMovieManagementRouteSuccess.routes ~> runRoute

        check {

          status shouldBe OK
          responseAs[MovieServiceApiResponse[MovieReservationInformationFO]] shouldEqual
            MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue), Some(moviereservationFo))
        }(result)


      }

      s"respond with HTTP-${OK} to book a seat for the movie and update its seat count for imdbId and screenId " in new scoping {


        val movieToReserve = HttpEntity(MediaTypes.`application/json`, MovieReservationtestDataUtils.reservationJson(movieInfo))

        val result = Post(url, movieToReserve) ~> mockReservationRouteUpdateCase.routes ~> runRoute

        check {

          status shouldBe OK
          responseAs[MovieServiceApiResponse[MovieReservationInformationFO]] shouldEqual
            MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue), Some(moviereservationFo.copy(reservedSeats = moviereservationFo.reservedSeats + 1)))
        }(result)


      }



      s"respond with HTTP-${BadRequest} for the situation when no seats are available " in new scoping {

        val movieToRegister = HttpEntity(MediaTypes.`application/json`, MovieReservationtestDataUtils.reservationJson(movieInfo))
        val result = Post(MovieReservationtestDataUtils.generalUrl, movieToRegister) ~> mockMovieResrvationRouteValidationFailure1.routes ~> runRoute


        check {
          status shouldBe BadRequest
          responseAs[MovieServiceApiResponse[String]] shouldEqual
          MovieServiceApiResponse(MovieServiceApiResponseMetaData(BadRequest.intValue, Some(MovieReservationManager.AllSeatReservedForMovie)))
        }(result)

      }
      s"respond with HTTP-${BadRequest} when a movie that is not registered is provided for booking" in new scoping {

        import MovieReservationManager._

        val movieToReserve = HttpEntity(MediaTypes.`application/json`, MovieReservationtestDataUtils.reservationJson(movieInfoWrong))

        val result = Post(MovieReservationtestDataUtils.generalUrl, movieToReserve) ~> mockMovieReservationRouteValidationFailure2.routes ~> runRoute


        check {
          status shouldBe BadRequest
          responseAs[MovieServiceApiResponse[String]] shouldEqual
            MovieServiceApiResponse(MovieServiceApiResponseMetaData(BadRequest.intValue, Some(MovieReservationValidationManager.MovieNotRegistered)))
        }(result)


      }
      s"respond with HTTP-${OK} and the movieReservationInformation when movie is searched with imdbID and screenId and the result is found" in new scoping{
        val url= s"${MovieReservationtestDataUtils.generalUrl}?imdbId=${movieInfo.imdbId}&screenId=${movieInfo.screenId}"
        val result = Get(url)~>movieReservationInformationRetrieval.routes~> runRoute
      check{
          status shouldBe OK
          responseAs[MovieServiceApiResponse[List[MovieReservationInformationRM]]] shouldEqual
            MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue),Some(List(movieReservationRM)))
        }(result)
    }


    s"respond with HTTP-${OK} and empty response when movie is searched with imdbID and screenId and the result is not  found " in new scoping{



      val url= s"${MovieReservationtestDataUtils.generalUrl}?imdbId=${movieInfo.imdbId}&screenId=${movieInfo.screenId}"
      val result = Get(url)~>movieReservationInfoRertrievalNoResponse.routes~> runRoute

        check{
          status shouldBe OK
          responseAs[MovieServiceApiResponse[List[MovieReservationInformationRM]]] shouldEqual
            MovieServiceApiResponse(MovieServiceApiResponseMetaData(OK.intValue),Some(List[MovieReservationInformationRM]()))
        }(result)
    }

    }


  }

