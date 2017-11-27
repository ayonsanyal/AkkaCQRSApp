package utils

import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.MovieInformation



/**
  * Created by AYON SANYAL on 21-09-2017.
  */


  object TestData {

    def movieInfo(
                   imdbId: String ,
                   screenId: String ,
                   availableSeats: Int

                 ): MovieInformation =
      MovieInformation(
        imdbId = imdbId,
         availableSeats = availableSeats,
        screenId = screenId)
  val movieInfo1 = movieInfo( "tt0111161", "screen_123456", 3)

  val movieInfoWrong = MovieInformation("tt01",100, "screen_123456")

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


