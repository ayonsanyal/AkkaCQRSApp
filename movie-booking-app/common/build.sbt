name := "movie-service-common"

libraryDependencies ++= {
  val akkaVersion = "2.5.6"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.14",
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,

    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11.2",
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11.2",
    "ch.qos.logback" % "logback-classic" % "1.0.9",
    "org.json4s" %% "json4s-ext" % "3.2.9",
    "org.json4s" %% "json4s-native" % "3.2.9",
    "com.google.protobuf" % "protobuf-java"  % "3.3.1",
    "com.typesafe.akka" %% "akka-http-testkit" %"10.0.10"


  )
}