package com.ayon.movieservice.movieregistration

import akka.actor.ActorSystem
import com.ayon.movieservice.common.{Bootstrap, MovieServiceRoutesDefinition}
import com.ayon.movieservice.movieregistration.route.MovieManagementRoutes
import com.ayon.movieservice.movieregistration.service.{MovieInformationManager}

/** A boot class which loads movie registration routes with required
  * dependencies.
  * Created by AYON SANYAL
  */
class MovieRegistrationBoot  extends Bootstrap{

  override def bootup(system: ActorSystem) = {

    import system.dispatcher
    val movieInfoManager = system.actorOf(MovieInformationManager.props,MovieInformationManager.Name)


    List(new MovieManagementRoutes(movieInfoManager))
  }
}
