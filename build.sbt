import Dependencies._

ThisBuild / scalaVersion     := "2.13.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.cowsunday"
ThisBuild / organizationName := "cowsunday"

lazy val root = (project in file("."))
  .settings(
    name := "silver",
    mainClass := Some("com.cowsunday.silver.Main"),
    libraryDependencies += javaFx,
  )