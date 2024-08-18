ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "tg-message-counter"
  )

libraryDependencies += "com.lihaoyi" %% "upickle" % "3.3.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.10.1"
