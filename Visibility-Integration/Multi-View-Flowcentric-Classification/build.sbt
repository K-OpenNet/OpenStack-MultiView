name := "Multi-View-Flowcentric-Classification"

version := "0.1"

scalaVersion := "2.11.6"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0"
libraryDependencies += "org.elasticsearch" %% "elasticsearch-spark-20" % "6.4.1"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.20.0" % "provided"