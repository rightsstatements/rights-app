# rights-app

For background information, see the [Request for Proposals for
RightsStatements.org](http://pro.europeana.eu/page/request-for-proposals-for-rightsstatements-org).

## Setup

Clone this repository

    $ git clone git@github.com:graphthinking/rights-app.git && cd rights-app

Init and update submodules

    $ git submodule init && git submodule update

Make sure you have a
[JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [sbt](http://www.scala-sbt.org/download.html) installed. Then

    $ sbt run

and visit [http://localhost:9000](http://localhost:9000).
