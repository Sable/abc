#!/bin/sh
./run_cases.pl ajcTests.xml &&
cp failed.xml failed_current.xml &&
cp passed.xml passed_current.xml;