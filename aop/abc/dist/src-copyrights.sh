#!/bin/sh -e

for d in `find src/ -name \*.java` src/abc/aspectj/parse/aspectj.flex src/abc/aspectj/parse/aspectj.ppg
   do perl -i dist/copyright.pl $d
done
