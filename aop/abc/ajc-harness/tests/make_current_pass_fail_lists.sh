#! /bin/sh

./run_cases.pl ajcTests.xml &&
cp failed.xml failed_current.xml &&
cp passed.xml passed_current.xml &&
cp skipped.xml skipped_current.xml &&
cp failed.output failed_current.output &&
cp failed_check.xml  failed_check_current.xml && 
cp passed_check.xml  passed_check_current.xml;

