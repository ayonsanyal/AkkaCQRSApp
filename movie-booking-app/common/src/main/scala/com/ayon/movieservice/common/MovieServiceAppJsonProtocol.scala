package com.ayon.movieservice.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import java.util.Date

/**
  * Created by AYON SANYAL on 18-11-2017
 * Base  class representing the Json protocol for others to extend from
 */
trait MovieServiceAppJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol{
  implicit object DateFormat extends JsonFormat[Date] {
    def write(date : Date) : JsValue = JsNumber(date.getTime)
    def read(json: JsValue) : Date = json match {
      case JsNumber(epoch) => new Date(epoch.toLong)
      case unknown => deserializationError(s"Expected JsString, got $unknown")
    }
  }
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(data: Any) = data match {
      case number: Int => JsNumber(number)
      case string: String => JsString(string)
      case bool: Boolean if bool == true => JsTrue
      case bool: Boolean if bool == false => JsFalse
    }
    def read(value: JsValue) = value match {
      case JsNumber(number) => number.intValue()
      case JsString(string) => string
      case JsTrue => true
      case JsFalse => false
    }
  }  
}