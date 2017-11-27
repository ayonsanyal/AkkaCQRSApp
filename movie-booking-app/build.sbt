import NativePackagerHelper._

name := "moviebookingapp"


lazy val commonSettings = Seq(
  organization := "com.ayon",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).
  aggregate(common, movieManagement, movieReservation, server)

lazy val common = (project in file("common")).
  settings(commonSettings: _*)

lazy val movieManagement = (project in file("movie-management")).
  settings(commonSettings: _*).
  dependsOn(common)

lazy val movieReservation = (project in file("movie-reservation")).
  settings(commonSettings: _*).
  dependsOn(common,movieManagement)





lazy val server = {
  import com.typesafe.sbt.packager.docker._
  Project(
    id = "server",
    base = file("server"),
    settings = commonSettings ++ Seq(
      mainClass in Compile := Some("com.ayon.movieservice.server.Server"),
      dockerCommands := dockerCommands.value.filterNot {
        // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
        case Cmd("USER", args@_*) => true
        // dont filter the rest
        case cmd => false
      },
      version in Docker := "latest",
      dockerExposedPorts := Seq(8083),

      dockerBaseImage := "java:8"
    )
  )
  .dependsOn(common, movieManagement, movieReservation)
  .enablePlugins(JavaAppPackaging)
}