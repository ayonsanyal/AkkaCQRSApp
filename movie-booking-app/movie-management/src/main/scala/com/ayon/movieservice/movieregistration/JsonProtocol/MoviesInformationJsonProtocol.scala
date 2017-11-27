package com.ayon.movieservice.movieregistration.JsonProtocol

import com.ayon.movieservice.common.MovieServiceApiResponseJsonProtocol
import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.MovieInformation


/**
  * Created by AYON SANYAL
  */
trait MoviesInformationJsonProtocol extends MovieServiceApiResponseJsonProtocol {
  implicit val movieInformationFO= jsonFormat6(MovieInformationFO.apply)
  implicit  val movieInformation= jsonFormat3(MovieInformation.apply)


}
