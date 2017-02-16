import sbt.Keys._


lazy val stream_example = (project in file("."))
  .settings(
    name := "stream_example",
    version := "1.0",
    scalaVersion := "2.11.8",
    trapExit := false,
    libraryDependencies := Seq(
      "org.scalatest" % "scalatest_2.11" % "3.0.0"
    )
  )