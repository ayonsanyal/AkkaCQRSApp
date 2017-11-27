package com.ayon.movieservice.moviereservation.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ayon.movieservice.common.MovieServiceRoutesDefinition
import com.ayon.movieservice.moviereservation.JsonProtocol.MovieReservationJsonProtocol
import com.ayon.movieservice.moviereservation.persistence.MovieReservationInformationFO
import com.ayon.movieservice.moviereservation.service.MovieReservationInformationRM
import com.ayon.movieservice.moviereservation.service.MovieReservationInformationView.FindMovieReservationByImDBIDAndScreenID
import com.ayon.movieservice.moviereservation.service.MovieReservationManager.{MovieReservationInformation, ReserveMovieSeat}

import scala.concurrent.ExecutionContext

/**
  * Created by AYON SANYAL on 25-11-2017
  */
class MovieReservationRoutes(movieReservationManager:ActorRef,movieReservationView:ActorRef)(implicit val executionContext: ExecutionContext) extends MovieServiceRoutesDefinition with MovieReservationJsonProtocol {

  def routes(implicit actorSystem: ActorSystem, executionContext: ExecutionContext, materializer: Materializer): Route = {
    import akka.http.scaladsl.server.Directives._
    pathPrefix("reservation") {

      pathEndOrSingleSlash {
        get {
          parameter('imdbId, 'screenId) { (imdbId, screenId) =>
            serviceAndComplete[List[MovieReservationInformationRM]](FindMovieReservationByImDBIDAndScreenID(imdbId, screenId), movieReservationView)
          }
        } ~
          post {
            entity(as[MovieReservationInformation]) { information =>
              serviceAndComplete[MovieReservationInformationFO](ReserveMovieSeat(information), movieReservationManager)

            }

          }


      }


    }


  }
}