package com.ayon.movieservice.movieregistration.persistence

import akka.actor.{ActorRef, Props}
import com.ayon.movieservice.common._
import com.ayon.movieservice.movieregistration.MovieRegistrationDatamodel






object MovieInformationFO{
  def empty = MovieInformationFO("","", 0, "","")
}

/**
 * Value object representation of a Movie
 */
case class MovieInformationFO(id:String,imdbId:String, availableSeats:Int,screenId:String,movieTitle:String,deleted:Boolean = false) extends EntityFieldsObject[String, MovieInformationFO]{
  def assignId(id:String) = this.copy(id = id)
  def markDeleted = this.copy(deleted = true)


}


/**
 * Entity class representing a Movie For Registration in MovieBookingApp
 */
class Movie(id:String) extends MovieServicePersistentEntity[MovieInformationFO](id){
  import Movie._
  import Command._
  import Event._


  override def initialState: MovieInformationFO = MovieInformationFO.empty
  override def snapshotAfterCount = Some(50)

  /**
    * This partial function covers the custom logic for command handling
    * in order to persist the command into journal.
    * @return
    */
  def additionalCommandHandling:Receive = {
    case RegisterMovie(movie) =>
      if (state != initialState){
        sender() ! Failure(FailureType.Validation, movieAlreadyRegistered)
      }
      else {
        log.info("persisting entity {}",movie)
        persist(MovieRegistered(movie))(handleEventAndRespond())
      }

  }
  
  def isCreateMessage(cmd:Any):Boolean = {
    cmd match {
      case regMovie:RegisterMovie => {

        true
      }
      case _ => {

        false
      }

    }
  }


  /**
    * Logic is defined for the event to occured after persistence.
    * @param event
    */
  def handleEvent(event:EntityEvent):Unit = event match {
    case MovieRegistered(movie) =>
      log.info("{} event created" ,event)
      state = movie

  }
}

/**
 * Companion to the Movie entity where the vocab is defined
 */
object Movie{

  val EntityType = "movieregistration"


  object Command{
    case class RegisterMovie(registrationDetails:MovieInformationFO)

  }
  
  object Event{
    trait MovieRegistrationEvent extends EntityEvent{def entityType = EntityType}
    case class MovieRegistered(movie:MovieInformationFO) extends MovieRegistrationEvent{
      def toDatamodel = {
        val movieDataModel = MovieRegistrationDatamodel.MovieRegistration.newBuilder().
          setId(movie.id).
          setImdbId(movie.imdbId).
          setAvailableSeats(movie.availableSeats).
          setScreenId(movie.screenId).
          setMovieTitle(movie.movieTitle).
          build

        MovieRegistrationDatamodel.MovieRegistered.newBuilder.setMovieregistration(movieDataModel).build
      }
    }
    object MovieRegistered extends DatamodelReader{
      def fromDatamodel = {
        case movieRegistered : MovieRegistrationDatamodel.MovieRegistered =>
          val movieDataModel = movieRegistered.getMovieregistration
          val movie = MovieInformationFO(movieDataModel.getId,movieDataModel.getImdbId, movieDataModel.getAvailableSeats,movieDataModel.getScreenId,movieDataModel.getMovieTitle)
          MovieRegistered(movie)
      }
    }





  }

  def props(id:String) = Props(classOf[Movie], id)

  val movieAlreadyRegistered = ErrorMessage("movie.alreadyexists", Some("This movie has already been registered and can not handle another registration request"))

}