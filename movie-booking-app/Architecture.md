                                  Architecture And Details for this Application
                                  
                                  
                                  
 1. This app shows a demo movie booking functionality where a movie can be registered and seat can be reserved for the movie.
 2. This application has rest end points for registration and reservation .
 3. Rest points are developed using AKKA HTTP.
 4. Akka Persistence has been used to persist the events as a part of Event Sourcing and CQRS.
 5. Cassandra is the primary database for persisting the events.
 6. Elastic Search is the read side database for faster search results.
 7. Akka Stream is used for the purpose of streaming  the events from the journal.
 8. For the purpose of event sourcing, the data model is separate from domain model.
 9. DataModel has two parts 1.WriteSide 2.ReadSide.
 10.DataModel uses Protobuff as a serializer.
 11.The actor model uses hierarchy based models in every module where 1 parent actor i.e service actor aka Manager in our application
    which creates child actors and delegates the request for further processing or validation and after everything is done ,respond routing actors with 
    corresponding results.
  12. The events get persisted into cassandra ,the view buildes streams and queries the persisted events by tag and  stores in indexes of elastic searchand creates the view.
  13.The view is used by ReadModel for read operations.
    
    
    
    Limitations:
    1.This app is reactive ,but not microservice based.
    2.A demo app which needs to be modified to be a prod ready app.
    
    Things To Be Done:
    1.Need to use cluster sharding and cluster singleton for scalability and availability of persistence actors and views.
    2.Need to change this monolith into microservice.
    3.Need to change replace akka event stream with akka stream kafka for better performance.
    
    References:
    The architecture is heavily inspired from 
    1.Mastering Akka -Christian Baxter.
    2.Reactive Messaging Patterns -Vaughn Vernon
    
    
    
    
    

       

     
     


     


    
    
