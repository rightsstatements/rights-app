#!/bin/sh

tx pull -f -a

for f in conf/messages_*.properties; do
  native2ascii $f $f
done

git add conf/messages_*.properties
git commit -m "Update i18n"
