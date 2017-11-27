package com.ayon.movieservice.server

import akka.actor._
import com.typesafe.config.ConfigFactory

import collection.JavaConversions._
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.Http
import com.ayon.movieservice.common.Bootstrap


/**Created by AYON SANYAL on 25-11-2017
 * Main entry point to startup the application
 */
object Server extends App{
  import akka.http.scaladsl.server.Directives._
  val conf = ConfigFactory.load.getConfig("moviebookingapp")
  
  implicit val system = ActorSystem("Moviebookingapp", conf)
  implicit val mater = ActorMaterializer()
  val log = Logging(system.eventStream, "Server")

  import system.dispatcher

  //Boot up each service module from the config and get the routes
  val routes = 
    conf.
      getStringList("serviceBoots").
     map(toBootClass).
      flatMap(_.bootup(system)).
      map(_.routes)
  val definedRoutes = routes.reduce(_~_)
  val finalRoutes = 
    pathPrefix("v1")(definedRoutes)
 val apiroutes=pathPrefix("api")(finalRoutes)
  
  
  val serverSource =
    Http().bind(interface = "0.0.0.0", port = 8080)
  val sink = Sink.foreach[Http.IncomingConnection](_.handleWith(apiroutes))
  serverSource.to(sink).run  
  
  def toBootClass(bootPrefix:String) = {
    val clazz = s"com.ayon.movieservice.${bootPrefix.toLowerCase}.${bootPrefix}Boot"
    Class.forName(clazz).newInstance.asInstanceOf[Bootstrap]
  }
}

