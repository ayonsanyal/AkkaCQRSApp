package com.ayon.movieservice.common

import akka.actor._

import scala.concurrent.Future

/**
  * Created by AYON SANYAL on 18-11-2017
 * Base actor definition for other actors in the movie service app to extend from
 */
trait MovieServiceManager extends Actor with ActorLogging{
  import akka.pattern.pipe
  import context.dispatcher
  
  //Partial Function  to be used with the .recover combinator to convert an exception on a failed Future into a
  //Failure ServiceResult
  private val toFailure:PartialFunction[Throwable, ServiceResult[Nothing]] = {
    case ex => Failure(FailureType.Service, ServiceResult.UnexpectedFailure, Some(ex))
  }
  
  /**
   * Pipes the response from a request to a service actor back to the sender, first
   * converting to a ServiceResult per the contract of communicating with a movie service
   * @param f The Future to map the result from into a ServiceResult
   */
  def pipeResponse[T](f:Future[T]):Unit = 
    f.
      map{
        case o:Option[_] => {
          log.info("received {}",o.getOrElse("error"))
          ServiceResult.fromOption(o)
        }
        case f:Failure =>{
          log.info("received success result with failure {}",f.message)
          f
        }
        case other => {
          log.info("received success result {}",other)
          CompleteResult(other)}
      }.
      recover(toFailure). 
      pipeTo(sender())
}