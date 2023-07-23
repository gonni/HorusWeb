val ScalatraVersion = "2.8.0"
val akkaVersion = "2.5.26"
val akkaHttpVersion = "10.1.11"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "com.yg"

classpathTypes += "maven-plugin"
resolvers += "jitpack" at "https://jitpack.io"

lazy val hello = (project in file("."))
  .settings(
    name := "space",
    version := "0.1.0-SNAPSHOT",
    assembly / mainClass := Some("com.yg.JettyLaunchMain"),
    assembly / assemblyJarName := "horus_view.jar",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "org.scalatra" %% "scalatra-json" % "2.8.2",
      "org.json4s"   %% "json4s-jackson" % "4.0.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.35.v20201120" % "container;compile",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
      "com.typesafe.slick" %% "slick" % "3.3.2",
//      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
      "com.h2database" % "h2" % "1.4.196",
      "com.mchange" % "c3p0" % "0.9.5.2",
      //"mysql" % "mysql-connector-java" % "5.1.44",
      "mysql" % "mysql-connector-java" % "8.0.27",
      "org.scalatra" %% "scalatra-auth" % "2.8.2",
      "org.scalatra" %% "scalatra-forms" % "2.8.2",
      // akka + influxdb
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.influxdb" % "influxdb-client-scala_2.13" % "6.0.0",
      "org.json4s" %% "json4s-jackson" % "4.1.0-M1",
      "com.typesafe" % "config" % "1.4.2",
      // ---- DL M1-----
      "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1",
//      "org.nd4j" % "nd4j-native" % "1.0.0-M2.1" % "macosx-arm64",
//      "org.bytedeco" % "openblas" % "0.3.21-1.5.8" % "macosx-arm64",
      // ---------------
      "org.datavec" % "datavec-api" % "1.0.0-M2.1",
      "org.datavec" % "datavec-data-image" % "1.0.0-M2.1",
      "org.datavec" % "datavec-local" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-datasets" % "1.0.0-M2.1",
      "org.deeplearning4j" % "resources" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-ui" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-zoo" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-parallel-wrapper" % "1.0.0-M2.1",
      "jfree" % "jfreechart" % "1.0.13",
      "org.jfree" % "jcommon" % "1.0.23",
      "org.apache.httpcomponents" % "httpclient" % "4.3.5",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "org.bytedeco" % "javacv-platform" % "1.5.5",
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.8.0-M1",
      "org.junit.jupiter" % "junit-jupiter-api" % "5.8.0-M1",
      "com.opencsv" % "opencsv" % "3.10",
      "org.projectlombok" % "lombok" % "1.18.26" % "provided"
    )
  )

libraryDependencies += "com.github.shin285" % "KOMORAN" % "3.3.9"
dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.0.0"
//libraryDependencies += "dev.zio" %% "zio" % "2.0.6"
//libraryDependencies += "dev.zio" %% "zio-streams" % "2.0.6"

enablePlugins(SbtTwirl)
enablePlugins(JettyPlugin)

containerPort in Jetty := 18090

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("application.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}

//resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "webapp"