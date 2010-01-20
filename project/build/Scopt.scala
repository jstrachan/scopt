import sbt._

class ScoptProject(info: ProjectInfo) extends DefaultProject(info) {

  val newReleaseToolsRepository = "Scala Tools Repository" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  //val scalatest = "org.scalatest" % "scalatest" % "1.0.1-for-scala-2.8.0.Beta1-RC5-with-test-interfaces-0.2-SNAPSHOT" % "test"

  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
}
