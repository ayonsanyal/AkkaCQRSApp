package com.ayon.movieservice.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json._


/**
  * Created by AYON SANYAL on 17-11-2017
 * Structure of a  response from a REST api call.  It has metadata  as well as the optional
 * response payload data if there was no error
 */

case class MovieServiceApiResponse[T](meta:MovieServiceApiResponseMetaData, response:Option[T] = None)

/**
 * Detailed information(Meta Data) about the response that will contain status code and any error information if there was an error
 */
case class MovieServiceApiResponseMetaData(statusCode:Int, error:Option[ErrorMessage] = None)

/**
 * Json protocol class for the api response set of types
 */
trait  MovieServiceApiResponseJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val errorMessageFormat = jsonFormat3(ErrorMessage.apply)
  implicit val metaFormat = jsonFormat2(MovieServiceApiResponseMetaData)
  implicit def serviceapiResponseFormat[T : JsonFormat] = jsonFormat2(MovieServiceApiResponse.apply[T])
}