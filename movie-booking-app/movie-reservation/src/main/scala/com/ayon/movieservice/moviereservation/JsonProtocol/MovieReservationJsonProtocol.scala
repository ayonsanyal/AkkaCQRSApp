package com.ayon.movieservice.moviereservation.JsonProtocol

import com.ayon.movieservice.common.{MovieServiceApiResponseJsonProtocol, MovieServiceAppJsonProtocol}
import com.ayon.movieservice.moviereservation.persistence.MovieReservationInformationFO
import com.ayon.movieservice.moviereservation.service.MovieReservationInformationRM
import com.ayon.movieservice.moviereservation.service.MovieReservationManager.MovieReservationInformation
import com.ayon.movieservice.moviereservation.service.MovieReservationViewBuilder.MovieReservationRM

/**
  * Created by AYON SANYAL on 18-09-2017.
  */
trait MovieReservationJsonProtocol extends MovieServiceApiResponseJsonProtocol
{

 implicit val movieReservationInformation = jsonFormat2(MovieReservationInformation.apply)
  implicit val movieReservationInformationFO= jsonFormat7(MovieReservationInformationFO.apply)
  implicit val movieReservationInformationRM=  jsonFormat6(MovieReservationRM.apply)
 implicit val movieReservationInfoRM =      jsonFormat5(MovieReservationInformationRM.apply)

}
