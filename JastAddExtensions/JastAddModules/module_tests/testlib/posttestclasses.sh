#!/bin/sh
dos2unix out$1 2>/dev/null
dos2unix out$1.default 2>/dev/null
diff out out$1.default

cd classes
find -maxdepth 1 -name "Main.class"  -execdir java -cp "../../../classes;." Main 2>&1 > out ';'

dos2unix out 2>/dev/null
dos2unix ../classes.default/out 2>/dev/null
diff out ../classes.default/out

cd ..
