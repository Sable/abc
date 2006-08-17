#!/bin/sh -e

FILES="full_current.output failed_current.output \
      failed_current.xml passed_current.xml \
      skipped_current.xml"

cd ../../
CLASSPATH= ant clobber
CLASSPATH= ant jars
cd ajc-harness/tests
nice -n 5 ./runtests abcTests.xml
cvs update $FILES
# perl -pe's/\&/\&amp;/g' -i passed.xml failed.xml skipped.xml
./calc_updates.pl "$1" "$2"
