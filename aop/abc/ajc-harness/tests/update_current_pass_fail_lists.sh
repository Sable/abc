#! /bin/sh

cd ../../ &&
ant >compile.output 2>&1 &&
cd ajc-harness/tests &&
nice -n 5 ./run_cases.pl ajcTests.xml >full.output 2>&1 &&
./calc_updates.pl | mail -s"test run results" &&
cp failed.xml failed_current.xml &&
cp passed.xml passed_current.xml &&
cp failed.output failed_current.output &&
cp full.output full_current.output &&
cvs commit -m"automatic update" full_current.output failed_current.output failed_current.xml passed_current.xml
