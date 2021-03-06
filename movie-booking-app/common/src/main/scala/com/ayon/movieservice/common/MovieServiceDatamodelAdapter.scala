package com.ayon.movieservice.common

import akka.persistence.journal.EventAdapter
import akka.persistence.journal.EventSeq
import com.google.protobuf.Message
import akka.persistence.journal.Tagged

/**
  * Created by AYON SANYAL on 18-11-2017
 * Trait for a class that can write it's state into a protobuf message
 */
trait DatamodelWriter{
  
  /**
   * Convert this domain model object's state into a protobuf message
   * @return the protobuf message for this object
   */
  def toDatamodel:Message
}

/**
 * Trait for a class that can read a protobuf message and produce a corresponding
 * domain model object
 */
trait DatamodelReader{
  
  /**
   * Returns a partial function for converting into the domain model
   * @return the partial function for handling the conversion
   */
  def fromDatamodel:PartialFunction[Message,AnyRef]
}

/**
 * Generic adapter class that will convert to and from the journal via protobuf
 */
class MovieServiceDatamodelAdapter extends EventAdapter{
  override def manifest(event:Any) = event.getClass.getName
    
  override def toJournal(event:Any) = event match {
    case ev:EntityEvent with DatamodelWriter => 
      val message = ev.toDatamodel
      
      //Add tags for the entity type and the event class name
      val eventType = ev.getClass.getName().toLowerCase().split("\\$").last
      Tagged(message, Set(ev.entityType, eventType))      
      
    case _ => throw new RuntimeException(s"Protobuf adapter can't write adapt type: $event")
  }
  
  override def fromJournal(event:Any, manifest:String) = {
    event match{
      case m:Message =>
        //Reflect to get the companion for the domain class that was serialized and then
        //use that to perform the conversion back into the domain model
        val reader = Class.forName(manifest + "$").getField("MODULE$").get(null).asInstanceOf[DatamodelReader]
        reader.
          fromDatamodel.
          lift(m).
          map(EventSeq.single).
          getOrElse(throw readException(event))        
        
      case _ => throw readException(event)
    }    
  }
  
  private def readException(event:Any) = new RuntimeException(s"Protobuf adapter can't read adapt for type: $event")
}