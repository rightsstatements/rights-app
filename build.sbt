name := "rights-app"

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

routesGenerator := InjectedRoutesGenerator

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "2.13.0"
libraryDependencies += "com.github.jknack" % "handlebars" % "2.2.2"
