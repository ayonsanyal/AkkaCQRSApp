package com.ayon.movieservice.movieregistration.service

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.ayon.movieservice.common._
import com.ayon.movieservice.movieregistration.persistence.Movie.Event.MovieRegistered
import com.ayon.movieservice.movieregistration.persistence.{Movie, MovieInformationFO}
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager.{FindMovieTitle, MovieFoundWithTitle, MovieTitleResponse}
import com.typesafe.config.ConfigFactory


/** Companion of MovieInformationManager
  * Created by AYON SANYAL on 18-11-2017
  */

object MovieInformationManager{
  val Name = "movie-information-manager"

  case class MovieInformation(imdbId:String,availableSeats:Int,screenId:String)
  case class RegisterNewMovie(information:MovieInformation)
  case class ValidationRequest(registerNewMovie: RegisterNewMovie)
  case class ValidatedRequest(information:MovieInformation,title:String)
  case class FindMovieById(id:String)
  def props = Props[MovieInformationManager]
  def movieRegistrationValidatorProps= Props[MovieRegistrationValidator]
  val movieAlreadyRegistered = ErrorMessage("movie.alreadyexists", Some("This movie has already been registered and can not handle another registration request"))
}

/** A service actor responsible for handling the execution of messages and logic for movie registration.
  * This actor will handle the write side operation for this module .
  * This actor will consult its child actors for validation and processing and then will delegate the response to route actor.
  *
  */
class MovieInformationManager extends Aggregate[MovieInformationFO,Movie] {
import MovieInformationManager._
import com.ayon.movieservice.common.MovieServicePersistentEntity._
import com.ayon.movieservice.movieregistration.persistence.Movie._
import Command._

import context.dispatcher

import concurrent.duration._



  override def receive ={

    case registerationReq:RegisterNewMovie=> {
      validator.forward(ValidationRequest(registerationReq))
      }



    case FindMovieById(id) =>
      log.info("Finding movie {}", id)
      forwardCommand(id,GetState)

    case ValidatedRequest(info,title)=>
      val id = s"${info.imdbId}${info.screenId}"
      val entity = lookupOrCreateChild(id)
      log.info("the sender is {}",sender())
       val caller = sender()
      val movieInfoFO = MovieInformationFO(id,info.imdbId, info.availableSeats, info.screenId, title)
      val command = RegisterMovie(movieInfoFO)
      entity.tell(command,caller)
  }



  def entityProps(id:String) = Movie.props(id)
  def lookup(name: String) = context.actorSelection(s"/user/$name")
  def validator = context.actorOf(movieRegistrationValidatorProps)
}
