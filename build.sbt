name := "acked-streams"

organization := "com.github.pjfanning"

scalaVersion := "2.13.12"

crossScalaVersions := Seq("2.12.18", "2.13.12", "3.3.1")

val appProperties = {
  val prop = new java.util.Properties()
  IO.load(prop, new File("project/version.properties"))
  prop
}

libraryDependencies ++= Seq(
  "org.apache.pekko"  %% "pekko-stream"  % "1.0.1",
  "org.scalatest"     %% "scalatest"     % "3.2.17" % Test)

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
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

Test / publishArtifact := false
