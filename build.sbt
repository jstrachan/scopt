name := "scopt"

version := "1.1.2"

organization := "com.github.scopt"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.1", "2.9.0-1", "2.8.1")

// junit
libraryDependencies += "junit" % "junit" % "4.7" % "test"

// scalatest
libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
	val versionMap = Map("2.8.1" -> "1.5.1", "2.9.0-1" -> "1.6.1", "2.9.1" -> "1.6.1")
	val libName =
	  if (List("2.8.1") contains sv) "scalatest_2.8.1" 
	  else "scalatest_2.9.0"
	val testVersion = versionMap.getOrElse(sv, error("Unsupported Scala version " + sv))
	deps :+ ("org.scalatest" % libName % testVersion % "test")
}

publishMavenStyle := true

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishTo <<= (version) { version: String =>
  val nexus = "http://nexus.scala-tools.org/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
  else                                   Some("releases" at nexus+"releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
