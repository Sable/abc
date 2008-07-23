#!/bin/sh
dos2unix out$1
dos2unix out$1.default
diff out out$1.default
find classes -name "*.class" -execdir jad '{}' ';' 2>&1 > /dev/null
diff -r classes$1 classes$1.default
