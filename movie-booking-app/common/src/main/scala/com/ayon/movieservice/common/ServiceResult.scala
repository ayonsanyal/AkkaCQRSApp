package com.ayon.movieservice.common

/**
  * Created by AYON SANYAL on 18-11-2017
 * Mentions a result from a call to a service actor, will be either a CompleteResult (Some), EmptyResult (None) or a Failure.
 * Contains  methods like map, flatMap and filter so that it can be treated as a monad.
 */
sealed abstract class ServiceResult[+A] {
  def isEmpty:Boolean
  def isValid:Boolean
  def getOrElse[B >: A](default: => B): B = default  
  def map[B](f: A => B): ServiceResult[B] = EmptyResult   
  def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = EmptyResult 
  def filter(p: A => Boolean): ServiceResult[A] = this 
  def toOption = this match{
    case CompleteResult(a) => Some(a)
    case _ => None
  }
}

/**
 * Companion to ServiceResult
 */
object ServiceResult{
  val UnexpectedFailure = ErrorMessage("common.unexpect", Some("An unexpected exception has occurred"))
  
  def fromOption[A](opt:Option[A]):ServiceResult[A] = opt match {
    case None => EmptyResult
    case Some(value) => CompleteResult(value)
  }
}

/**
 * Empty (negative) representation of a service call result.  For example, if looking up 
 * an entity by id and it wasn't there this is the type of response to use
 */
sealed abstract class Empty extends ServiceResult[Nothing]{
  def isValid:Boolean = false
  def isEmpty:Boolean = true
}
case object EmptyResult extends Empty

/**
 * Full (positive) representation of a service call result.  This will wrap the result of a call to service to qualify
 * to the receiver that the call was successful
 */
final case class CompleteResult[+A](value:A) extends ServiceResult[A]{
  def isValid:Boolean = true
  def isEmpty:Boolean = false
  override def getOrElse[B >: A](default: => B): B = value
  override def map[B](f: A => B): ServiceResult[B] = CompleteResult(f(value))
  override def filter(p: A => Boolean): ServiceResult[A] = if (p(value)) this else EmptyResult 
  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = f(value)   
}

/**
 * Represents the type of failure encountered by the app
 */
object FailureType extends Enumeration{
  val Validation, Service = Value
}

/**
 * Mentions an error message from a failure with a service call.  Consists of  fields for the code of the error
 * as well as a description of the error
 */
case class ErrorMessage(code:String, shortText:Option[String] = None, params:Option[Map[String,String]] = None)

/**
 * Companion object of ErrorMessage
 */
object ErrorMessage{
  /**
   * Common error where on a non existent entity,an operation is requested
   */
  val InvalidEntityId = ErrorMessage("invalid.entity.id", Some("No matching entity found"))
}

/**
 * Failure occured from a call to a service with fields for what type as well as the error message
 * and optionally a stack trace
 */
sealed case class Failure(failType:FailureType.Value, message:ErrorMessage, exception:Option[Throwable] = None) extends Empty{
  type A = Nothing
  override def map[B](f: A => B): ServiceResult[B] = this  
  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = this   
}
