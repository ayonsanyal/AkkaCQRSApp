package com.ayon.movieservice.movieregistration.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ayon.movieservice.common.MovieServiceRoutesDefinition
import com.ayon.movieservice.movieregistration.JsonProtocol.MoviesInformationJsonProtocol
import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.{MovieInformation, RegisterNewMovie}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

/** Routes for movie management module where,
  * the endpoints are defined for movie registration.
  * Created by AYON SANYAL on 25-11-2017
  */
class MovieManagementRoutes(movieInfoManager:ActorRef)(implicit val executionContext: ExecutionContext) extends MovieServiceRoutesDefinition with MoviesInformationJsonProtocol {



  def routes(implicit actorSystem: ActorSystem,executionContext: ExecutionContext,materializer: Materializer):Route ={
   import akka.http.scaladsl.server.Directives._



   pathPrefix("registration"){

        post{

         entity(as[MovieInformation]) { movieInfo =>
           serviceAndComplete[MovieInformationFO](RegisterNewMovie(movieInfo), movieInfoManager)
         }
       }

   }
 }
}
