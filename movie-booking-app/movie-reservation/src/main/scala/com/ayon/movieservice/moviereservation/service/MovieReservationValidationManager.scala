package com.ayon.movieservice.reservation.service

import akka.actor.{ActorIdentity, ActorRef, FSM, Identify, Props}
import com.ayon.movieservice.common._
import com.ayon.movieservice.movieregistration.persistence.MovieInformationFO
import com.ayon.movieservice.movieregistration.service.MovieInformationManager
import com.ayon.movieservice.movieregistration.service.MovieInformationManager.FindMovieById
import com.ayon.movieservice.moviereservation.persistence.MovieReservationInformationFO
import com.ayon.movieservice.moviereservation.service.MovieReservationManager.{ReserveMovieSeat, ValidatedReservationRequest}

import concurrent.duration._

/** Companion of  MovieReservationValidationManager
  * Created by AYON SANYAL on 17-11-2017.
  */

 object MovieReservationValidationManager{
  def props = Props[MovieReservationValidationManager]

  sealed trait State
  case object WaitingForData extends State
  case object ResolvingConsultingActors extends State
  case object PreparingValidatedResult extends State

  sealed trait Data{
    def inputs:Inputs
  }

  case object NoData extends Data{
    def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator:ActorRef, request:ReserveMovieSeat)

  trait InputsData extends Data{
    def inputs:Inputs
    def originator = inputs.originator
  }

  object ServiceIdentfier extends Enumeration{
    val Movie,MovieReservation= Value
  }

  case class DependencyToConsult(inputs:Inputs,movieInformationManager: Option[ActorRef]=None) extends  InputsData
  case class ConsultingDependency(inputs:Inputs,movieInformationManager:ActorRef,movieInfoFO:Option[MovieInformationFO]) extends InputsData



  val ResolveTimeout = 15 seconds
  val MovieNotRegistered = ErrorMessage("movie.not registered ", Some("This movie is not registered in our records so, can not handle reservation request"))
}


/**
  * Validation actor for movie reservation module responsible for data validation
  * required for movie reservation.
  * It is a FSM actor which process the data in different states where validation is performed.
  */
class MovieReservationValidationManager extends FSM[MovieReservationValidationManager.State,MovieReservationValidationManager.Data] {
 import MovieReservationValidationManager._


  startWith(WaitingForData,NoData)

  when(WaitingForData){
    case Event(request:ReserveMovieSeat,_)=>
      lookup(MovieInformationManager.Name) ! Identify(ServiceIdentfier.Movie)


      goto(ResolvingConsultingActors) using DependencyToConsult(Inputs(sender(), request))
  }
  when(ResolvingConsultingActors,ResolveTimeout)(transform{
    case Event(ActorIdentity(identifier:ServiceIdentfier.Value, actor @ Some(ref)),
    data:DependencyToConsult) =>

      val newData= identifier match {
        case ServiceIdentfier.Movie=> data.copy(movieInformationManager=actor)

      }
      stay using newData
  }
  using{
    case FSM.State(state,DependencyToConsult(inputs,Some(movieInformationManager)),_,_,_)=>
      val id = s"${inputs.request.information.imdbId}${inputs.request.information.screenId}"
      movieInformationManager ! FindMovieById(id)
      goto(PreparingValidatedResult) using(ConsultingDependency(inputs,movieInformationManager,None))
  } )

  when(PreparingValidatedResult,ResolveTimeout){
    case Event(CompleteResult(movieInformationFo:MovieInformationFO),data:ConsultingDependency)=>
      log.info("Found movie: {}", movieInformationFo)

      context.parent.tell(ValidatedReservationRequest(movieInformationFo),data.inputs.originator)
      stop

    case Event(EmptyResult,data:ConsultingDependency)=>

      data.originator ! Failure(FailureType.Validation,MovieNotRegistered)
      stop





  }







  whenUnhandled{
    case Event(StateTimeout , data) =>
      log.error("Received state timeout in process to validate an order create request")
      data.inputs.originator ! unexpectedFail
      stop

    case Event(other, data) =>
      log.error("Received unexpected message of {} in state {}", other, stateName)
      data.inputs.originator ! unexpectedFail
      stop
  }


  def unexpectedFail = Failure(FailureType.Service, ServiceResult.UnexpectedFailure )
  def lookup(name:String) = context.actorSelection(s"/user/$name")
}
