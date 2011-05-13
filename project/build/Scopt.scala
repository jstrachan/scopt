import sbt._

class ScoptProject(info: ProjectInfo) extends DefaultProject(info) {
  val scalaToolsNexusSnapshots = "Scala Tools Nexus Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val scalaToolsNexusReleases  = "Scala Tools Nexus Releases" at "http://nexus.scala-tools.org/content/repositories/releases/"
  
  def scalatest = crossScalaVersionString match {
    case "2.7.7" => "org.scalatest" % "scalatest" % "1.1" % "test"
    case "2.8.0" => "org.scalatest" % "scalatest" % "1.2" % "test"
    case "2.8.1" => "org.scalatest" % "scalatest" % "1.2" % "test"
    case _ => "org.scalatest" %% "scalatest" % "1.4.1" % "test"
  }
  
  val junit = "junit" % "junit" % "4.7" % "test"
  
  override def managedStyle = ManagedStyle.Maven
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  // val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}
