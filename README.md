# rights-app

This is the application that serves the vocabulary terms in the [rightsstatements.org data model](https://github.com/rightsstatements/data-model). For background information, see the [Requirements for the Technical Infrastructure for Standardized International Rights Statements](http://rightsstatements.org/en/documentation/technical-white-paper/) white paper.

## Setup

Clone this repository

    $ git clone git@github.com:rightsstatements/rights-app.git && cd rights-app

rights-app is built with [Play Framework](https://www.playframework.com/) 2.4.3 so you need to make sure to have the
[Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [sbt](http://www.scala-sbt.org/download.html) installed. Then

    $ sbt run

and visit [http://localhost:9000](http://localhost:9000).

## Implementing translations

[Transifex](https://www.transifex.com/rightsstatements-org/rightsstatementsorg/dashboard/) is used to carry out translations of the app. The relevant resource is tagged with the category `rights-app`. Please get in touch with the [maintainers](https://www.transifex.com/rightsstatements-org/rightsstatementsorg/settings/maintainers/) to add a new language to the project.

To incorporate updates or new translations, install [`tx`](https://docs.transifex.com/client/introduction). Unicode-only characters in the translated strings will need to be converted to ASCII for the application to support them. Also, single quotes will need to be escaped. For convenience, the [`updateI18n.sh` script](updateI18n.sh) performs this functionality for you. Arguments provided to the script are passed to [`tx pull`](https://docs.transifex.com/client/pull#command-options). To update every language, you can execute

    $ ./updateI18n.sh -f -a

and to add or update specific languages, you can separate them with commas, like so:

    $ ./updateI18n.sh -f -l fr,it

If a new translation has been added, enable it by editing the `languages.available` key in `conf/application.conf`.

The new version needs to be tagged with git so that it gets recognized as a new release, using a tag that starts with `v` (e.g. `v1.2.7`).

See also notes on implementing translations for the [data model](https://github.com/rightsstatements/data-model/blob/master/README.md) and the [website](https://github.com/rightsstatements/rightsstatements.github.io/blob/master/README.md) for more information.
