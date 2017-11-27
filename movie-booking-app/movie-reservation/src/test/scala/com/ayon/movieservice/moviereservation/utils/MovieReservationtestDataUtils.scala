package com.ayon.movieservice.moviereservation.utils

import com.ayon.movieservice.moviereservation.service.MovieReservationManager.MovieReservationInformation
import spray.json._

/**
  * Created by AYON SANYAL
  */
object MovieReservationtestDataUtils {

  def reservationJson(reservationInfo: MovieReservationInformation): String =
    s"""
       |{
       |"imdbId": "${reservationInfo.imdbId}",
       |"screenId": "${reservationInfo.screenId}"
       |}
       |""".stripMargin.parseJson.toString




  def generalUrl(implicit reservationInfo: MovieReservationInformation ): String =
    s"/reservation"
}
