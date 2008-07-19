#!/bin/sh
dos2unix out
dos2unix out.default
diff out out.default
find classes -name "*.class" -execdir jad '{}' ';' 2>&1 > /dev/null
diff -r classes classes.default
