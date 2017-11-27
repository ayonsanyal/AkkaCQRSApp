package com.ayon.movieservice.moviereservation.utils

import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.MovieInformation
import com.ayon.movieservice.moviereservation.persistence.MovieReservationInformationFO
import com.ayon.movieservice.moviereservation.service.MovieReservationInformationRM
import com.ayon.movieservice.moviereservation.service.MovieReservationManager.MovieReservationInformation


/**
  * Created by AYON SANYAL
  */


  object MovieReservationTestData {

    def moviereservationInfo(
                   imdbId: String ,
                   screenId: String


                 ): MovieReservationInformation =
      MovieReservationInformation(
        imdbId = imdbId,

        screenId = screenId




      )
  val moviereservationInfo1 = moviereservationInfo( "tt0111161", "screen_123456")
  val movieReservationInfoWrong= moviereservationInfo( "tt0111162", "screen_123456")

  val movieReservation2=moviereservationInfo("tt0111162","screen_123457")
  val movieReservation3=moviereservationInfo("tt0111164","screen_123457")

  def movieRservationInfoFo(imdbId: String ,
                           screenId: String ,
                           movieTitle:String,
                           availableSeats: Int,
                           reservedSeats:Int

                          ):MovieReservationInformationFO=
    MovieReservationInformationFO(
      id=s"${imdbId}${screenId}",
      imdbId = imdbId,
      screenId = screenId,
      movieTitle=movieTitle,
      availableSeats = availableSeats,
      reservedSeats=reservedSeats
    )

  def movieRservationInfoRM(imdbId: String ,
                            screenId: String ,
                            movieTitle:String,
                            availableSeats: Int,
                            reservedSeats:Int

                           ):MovieReservationInformationRM=
    MovieReservationInformationRM(
      imdbId = imdbId,
      screenId = screenId,
      movieTitle=movieTitle,
      availableSeats = availableSeats,
      reservedSeats=reservedSeats
    )

  val movieReservationInfoFO1= movieRservationInfoFo("tt0111161", "screen_123456", "The Shawshank Redemption",3,1)
  val movieReservationInfoFO2= movieRservationInfoFo("tt0111161", "screen_123456", "The Shawshank Redemption",3,3)
  val movieReservationInfoFO3= movieRservationInfoFo("tt0111161", "screen_123456", "The Shawshank Redemption",3,0)
  val movieReservationInfoRM= movieRservationInfoRM("tt0111161", "screen_123456", "The Shawshank Redemption",3,1)
  def movirRegistrationInfo(imdbId: String ,
                            screenId: String ,
                            movieTitle:String,
                            availableSeats: Int

                           ):MovieInformationFO=
    MovieInformationFO(
      id =s"${imdbId}${screenId}",
      imdbId = imdbId,
      availableSeats = availableSeats,
      screenId = screenId,
      movieTitle=movieTitle
    )

  val movieInfoFO1= movirRegistrationInfo("tt0111161", "screen_123456", "The Shawshank Redemption",3)




}


