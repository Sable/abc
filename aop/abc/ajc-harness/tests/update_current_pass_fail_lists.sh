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
./calc_updates.pl | mail -s"test run results" abc@comlab.ox.ac.uk
cp failed.xml failed_current.xml
cp passed.xml passed_current.xml
cp skipped.xml skipped_current.xml
cp failed.output failed_current.output
cp full.output full_current.output
cvs commit -m"automatic update" $FILES
