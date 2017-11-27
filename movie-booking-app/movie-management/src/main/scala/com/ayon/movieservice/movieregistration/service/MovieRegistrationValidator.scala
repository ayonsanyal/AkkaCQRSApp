package com.ayon.movieservice.movieregistration.service

import akka.actor.{ActorRef, FSM, Identify, Props}
import com.ayon.movieservice.common._
import com.ayon.movieservice.common.MovieServicePersistentEntity.GetState
import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.{RegisterNewMovie, ValidatedRequest, ValidationRequest}
import com.ayon.movieservice.movieregistration.service.MovieInformationValidationManager.{FindMovieTitle, MovieFoundWithTitle, MovieTitleNotFound}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/** Companion of MovieRegistrationValidator
  * Created by AYON SANYAL on 19-11-2017
  *
  *
  *
  */


object MovieRegistrationValidator{

  sealed trait State
  case object WaitingForData extends State
  case object CollectingMovieTitle extends State
  case object PreparingValidatedRequest extends State

  sealed trait Data{
    def inputs:Inputs
  }

  case object NoData extends Data{
    def inputs = Inputs(ActorRef.noSender,null)
  }

  case class Inputs(originator:ActorRef, request:ValidationRequest)



  trait InputsData extends Data{
    def inputs:Inputs
    def originator = inputs.originator

  }

  object ServiceIdentfier extends Enumeration{
    val Movie= Value
  }

  case class  GatherTitleForTheMovie(inputs:Inputs) extends InputsData
  case class  GatheringResults(inputs:Inputs,movieInformationFO: MovieInformationFO) extends InputsData
  val ResolveTimeout = 5 seconds
  val movieAlreadyRegistered = ErrorMessage("movie.alreadyexists", Some("This movie has already been registered and can not handle another registration request"))

}

/**
  * A validation actor responsible for validation of data before movie registration.
  * It is a FSM actor which does the validation in its subsequent states .
  */
class MovieRegistrationValidator extends  FSM[MovieRegistrationValidator.State,MovieRegistrationValidator.Data]{
  import MovieRegistrationValidator._




  startWith(WaitingForData,NoData)

  when(WaitingForData){
    case Event(request:ValidationRequest,_) =>

      goto(CollectingMovieTitle) using GatherTitleForTheMovie(Inputs(sender(),request))


  }


  when(CollectingMovieTitle,ResolveTimeout){


    case Event(_,data:GatherTitleForTheMovie)=>

      val movieURL = ConfigFactory.load.getString("myApiFilmsUrl")
      val token = ConfigFactory.load.getString("myApiFilmsToken")

      val url = s"$movieURL${data.inputs.request.registerNewMovie.information.imdbId}&token=$token"
      movieRegistrationValidationManagerActor.tell(FindMovieTitle(url),self)
      goto(PreparingValidatedRequest) using  GatheringResults(data.inputs,null)
  }

  when(PreparingValidatedRequest){
    case Event(event:CompleteResult[MovieFoundWithTitle],data:GatheringResults)=>
      context.parent.tell(ValidatedRequest(data.inputs.request.registerNewMovie.information,event.value.movieTitle),data.originator)
      stop
    case Event(fail:Failure,data:GatheringResults)=>
      data.originator ! fail
      stop



  }


  def movieRegistrationValidationManagerActor=context.actorOf(Props(classOf[MovieInformationValidationManager],self))

}
