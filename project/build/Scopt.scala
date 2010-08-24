import sbt._

class ScoptProject(info: ProjectInfo) extends DefaultProject(info) {

  val newReleaseToolsRepository = "Scala Tools Repository" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val junit = "junit" % "junit" % "4.7" % "test"
  
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
}
