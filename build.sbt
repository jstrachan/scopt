name := "scopt"

version := "1.1.2-SNAPSHOT"

organization := "com.github.scopt"

scalaVersion := "2.8.1"

crossScalaVersions := Seq("2.8.1", "2.9.0-1", "2.8.0", "2.7.7")

// junit
libraryDependencies += "junit" % "junit" % "4.7" % "test"

// scalatest
libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
	val versionMap = Map("2.7.7" -> "1.1", "2.8.0" -> "1.2", "2.8.1" -> "1.2", "2.9.0-1" -> "1.4.1")
	val libName =
	  if (List("2.7.7", "2.8.0", "2.8.1") contains sv) "scalatest" 
	  else "scalatest_2.9.0"
	val testVersion = versionMap.getOrElse(sv, error("Unsupported Scala version " + sv))
	deps :+ ("org.scalatest" % libName % testVersion % "test")
}

publishMavenStyle := true

publishTo <<= (version) { version: String =>
  val nexus = "http://nexus.scala-tools.org/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
  else                                   Some("releases" at nexus+"releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
