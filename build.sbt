val ScalatraVersion = "2.7.0"
val akkaVersion = "2.5.26"
val akkaHttpVersion = "10.1.11"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "com.yg"

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
      "com.typesafe" % "config" % "1.4.2"
    )
  )

enablePlugins(SbtTwirl)
enablePlugins(JettyPlugin)

containerPort in Jetty := 8090

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("application.conf") => MergeStrategy.concat
  case x => MergeStrategy.last
}

//resourceDirectory in Compile := {
//    baseDirectory.value / "src/main/webapp"
//}
