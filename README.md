# rights-app

This is the application that serves the vocabulary terms in the [rightsstatements.org data model](https://github.com/rightsstatements/data-model). For background information, see the [Requirements for the Technical Infrastructure for Standardized International Rights Statements](http://rightsstatements.org/en/documentation/technical-white-paper/) white paper.

## Setup

Clone this repository

    $ git clone git@github.com:graphthinking/rights-app.git && cd rights-app

rights-app is built with [Play Framework](https://www.playframework.com/) 2.4.3 so you need to make sure to have the
[Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [sbt](http://www.scala-sbt.org/download.html) installed. Then

    $ sbt run

and visit [http://localhost:9000](http://localhost:9000).

## Implemmenting translations

* To implement translations, copy and paste entries from the completed Google Doc for the destination language into a new file for that language (e.g. `conf/messages_nl.properties` for Dutch).
* Confirm that there are no non-breaking spaces (`\u00A0`), no "smart quotes", and no em- or en-dashes.
* All text should be in UTF-8 before proceeding.
* Once complete, ensure that any non-ASCII unicode characters are escaped by using `native2ascii`, e.g. `echo $(cat conf/messages_nl.conf) | native2ascii > conf/messages_nl.conf`
* Enable the translation by editing the `languages.available` key in `conf/application.conf`.

See also notes on implementing translations for the [data model](https://github.com/rightsstatements/data-model/blob/master/README.md) and the [website](https://github.com/rightsstatements/rightsstatements.github.io/blob/master/README.md) for more information.
