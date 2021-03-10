#!/bin/sh

tx pull "$@"

for f in conf/messages_*.properties; do
  native2ascii $f $f

  # java.text.MessageFormat uses single quotes to escape patterns that would
  # otherwise be used for parameter substitutions. To produce a single quote,
  # you need to escape it with itself.
  # See https://www.playframework.com/documentation/2.4.x/ScalaI18N#Notes-on-apostrophes
  sed -i "s/'/''/g" $f
done

echo "To commit i18n changes, use:"
echo ""
echo " $ git add conf/messages_*.properties"
echo " $ git commit -m \"Update i18n\""
