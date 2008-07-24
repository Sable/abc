#!/bin/sh
dos2unix out$1 2>/dev/null
dos2unix out$1.default 2>/dev/null
diff out out$1.default
find classes -name "*.class" -execdir jad '{}' 2>/dev/null ';' 
cd classes
find -name "*.jad" -exec diff '{}' ../classes.default/'{}' ';'
cd ..
