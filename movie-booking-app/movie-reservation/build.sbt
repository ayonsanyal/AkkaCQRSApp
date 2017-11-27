name := "movie-reservation"

val akkaVersion = "2.5.6"
resolvers += Resolver.jcenterRepo
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.typesafe.play" %% "play-json" % "2.6.0-M6",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" %"10.0.10",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.7"

)


PB.targets in Compile := Seq(
  PB.gens.java -> (sourceDirectory.value /"main/java")
)
