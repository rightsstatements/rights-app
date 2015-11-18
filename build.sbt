name := "rights-app"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "2.13.0"
