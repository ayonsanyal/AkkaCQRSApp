package com.ayon.movieservice.common

import akka.actor.ActorSystem

/**
 * Trait that defines a class that will boot up actors from within a specific services module
  * Created by AYON SANYAL on 21-11-2017
 */
trait Bootstrap{
  
  /**
   * It boots up the actors for a service module and returns the service endpoints for that
   * module to be included in the Unfiltered server as plans
   * @param system The actor system to boot actors into
   * @return a List of Movie Services to be added into server
   */
  def bootup(system:ActorSystem):List[MovieServiceRoutesDefinition]
}