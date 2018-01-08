# rights-app

This is the application that serves the vocabulary terms in the [rightssstatements.org data model](https://github.com/rightsstatements/data-model). For background information, see the [Requirements for the Technical Infrastructure for Standardized International Rights Statements](http://rightsstatements.org/en/documentation/technical-white-paper/) white paper.

## Setup

Clone this repository

    $ git clone git@github.com:graphthinking/rights-app.git && cd rights-app

rights-app is built with [Play Framework](https://www.playframework.com/) 2.4.3 so you need to make sure to have the
[Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [sbt](http://www.scala-sbt.org/download.html) installed. Then

    $ sbt run

and visit [http://localhost:9000](http://localhost:9000).
