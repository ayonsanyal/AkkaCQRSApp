package com.ayon.movieservice.moviereservation

import akka.actor.{ActorSystem, Props}
import com.ayon.movieservice.common.Bootstrap
import com.ayon.movieservice.moviereservation.routes.MovieReservationRoutes
import com.ayon.movieservice.moviereservation.service.{MovieReservationInformationView, MovieReservationManager, MovieReservationViewBuilder}
import com.ayon.movieservice.reservation.service.MovieReservationValidationManager

/**
  * Created by AYON SANYAL on 17-11-2017.
  */
class MovieReservationBoot extends Bootstrap{
  override def bootup(system: ActorSystem)={
    import system.dispatcher

    val movieReservationView= system.actorOf(MovieReservationInformationView.props,MovieReservationInformationView.Name)
    val movieReservationValidationManager = system.actorOf(Props[MovieReservationValidationManager])
    val movieReservationmanager =  system.actorOf(MovieReservationManager.props,MovieReservationManager.Name)
    system.actorOf(MovieReservationViewBuilder.props,MovieReservationViewBuilder.Name)
    List(new MovieReservationRoutes(movieReservationmanager,movieReservationView))
  }

}
