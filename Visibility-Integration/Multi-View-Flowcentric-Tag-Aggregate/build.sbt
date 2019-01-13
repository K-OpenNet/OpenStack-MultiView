name := "Multi-View-Flowcentric-Tag-Aggregate"

version := "0.1"

scalaVersion := "2.11.6"

//noinspection Annotator
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
//libraryDependencies += "org.elasticsearch" % "elasticsearch-hadoop" % "6.4.1" % "provided"
libraryDependencies += "org.elasticsearch" %% "elasticsearch-spark-20" % "6.4.1" % "provided"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.20.0" % "provided"
libraryDependencies += "org.mongodb.spark" %% "mongo-spark-connector" % "2.2.5" % "provided"
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.6.4" % "provided"