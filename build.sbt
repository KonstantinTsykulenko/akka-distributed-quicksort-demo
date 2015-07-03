name := "akka-distributed-quicksort-demo"

version := "1.0"

scalaVersion := "2.11.5"

val akkaVersion = "2.3.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion
)
