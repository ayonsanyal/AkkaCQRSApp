package utils

import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.MovieInformation
import spray.json._
/**
  * Created by AYON SANYAL on 21-09-2017.
  */
object TestDataUtils {

  def registrationJson(movieInformation: MovieInformation): String =
    s"""
       |{
       |"imdbId": "${movieInformation.imdbId}",
       |"availableSeats": ${movieInformation.availableSeats},
       |"screenId": "${movieInformation.screenId}"
       |}
       |""".stripMargin.parseJson.toString

  def registrationJsonWrong(movieInformation: MovieInformation):String=
    s"""
       |{
       |"imdbId": "${movieInformation.imdbId}",
       |"availableSeats": ${movieInformation.availableSeats},
       |"screenId": "${movieInformation.screenId}"
       |}
       |""".stripMargin.parseJson.toString

  def generalUrl(implicit movieInformation: MovieInformation ): String =
    s"/registration"

  def registrationConfirmationjson(movieInfoFo:MovieInformationFO):String=
    s"""
       |{
       |"imdbId": "${movieInfoFo.imdbId}",
       |"availableSeats": ${movieInfoFo.availableSeats},
       |"screenId": "${movieInfoFo.screenId}"
       |"movieTitle" :"${movieInfoFo.movieTitle}"
       |}
       |""".stripMargin.parseJson.toString
}
