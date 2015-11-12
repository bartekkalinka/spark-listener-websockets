name := "ws-broadcast"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaV = "2.3.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0"
  )
}

