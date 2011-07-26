#!/bin/sh -e

FILES="full_current.output failed_current.output \
      failed_current.xml passed_current.xml \
      skipped_current.xml"

ADDRESS=$1
SUBJECT=$2
shift 2

#cd ../../../abc
#CLASSPATH= ant clobber
#CLASSPATH= ant jars
#cd ../abc-testing/ajc-harness/tests
nice -n 5 ./runtests $* abcTests.xml # -morexml abcWholeProgramTests.xml 
svn update --trust-server-cert $FILES
# perl -pe's/\&/\&amp;/g' -i passed.xml failed.xml skipped.xml
./calc_updates.pl "$ADDRESS" "$SUBJECT"
