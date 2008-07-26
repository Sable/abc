#!/bin/sh
dos2unix out$1 2>/dev/null
dos2unix out$1.default 2>/dev/null
diff out out$1.default
find classes -name "*.class" -execdir jad '{}' 2>/dev/null ';' 
cd classes
find -name "Main.class" -execdir java -cp "../../../classes;." Main 2>&1 > out ';'

dos2unix out 2>/dev/null
dos2unix ../classes.default/out 2>/dev/null
diff out ../classes.default/out

find -name "*.jad" -exec diff '{}' ../classes.default/'{}' ';'
cd ..
