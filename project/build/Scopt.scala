import sbt._

class ScoptProject(info: ProjectInfo) extends DefaultProject(info) {
  val scalaToolsNexusSnapshots = "Scala Tools Nexus Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  val scalaToolsNexusReleases  = "Scala Tools Nexus Releases" at "http://nexus.scala-tools.org/content/repositories/releases/"
  
  val scalatest = "org.scalatest" % "scalatest" % scalatestVersion % "test"
  def scalatestVersion = crossScalaVersionString match {
    case "2.7.7" => "1.1"
    case _ => "1.2"
  }
  
  val junit = "junit" % "junit" % "4.7" % "test"
  
  override def managedStyle = ManagedStyle.Maven
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  // val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}
