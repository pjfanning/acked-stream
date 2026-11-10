name := "acked-streams"

organization := "com.github.pjfanning"

scalaVersion := "2.13.18"

crossScalaVersions := Seq("2.13.18", "3.3.9")

val appProperties = {
  val prop = new java.util.Properties()
  IO.load(prop, new File("project/version.properties"))
  prop
}

libraryDependencies ++= Seq(
  "org.apache.pekko"  %% "pekko-stream"  % "2.0.0",
  "org.scalatest"     %% "scalatest"     % "3.2.19" % Test)

version := appProperties.getProperty("version")

homepage := Some(url("https://github.com/pjfanning/acked-stream"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

pomExtra := {
  <scm>
    <url>https://github.com/pjfanning/acked-stream</url>
    <connection>scm:git:git@github.com:pjfanning/acked-stream.git</connection>
  </scm>
  <developers>
    <developer>
      <id>timcharper</id>
      <name>Tim Harper</name>
      <url>http://timcharper.com</url>
    </developer>
    <developer>
      <id>pjfanning</id>
      <name>PJ Fanning</name>
      <url>https://github.com/pjfanning</url>
    </developer>
  </developers>
}

publishMavenStyle := true

publishTo := {
  if (isSnapshot.value)
    Some("central-snapshots" at "https://central.sonatype.com/repository/maven-snapshots/")
  else
    localStaging.value
}

Test / publishArtifact := false
