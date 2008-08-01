#!/bin/sh
dos2unix out 2>/dev/null
dos2unix out.default 2>/dev/null
diff out out.default

cd classes
find -name "Main.class" -execdir java -cp "$1;../../../classes;." Main 2>&1 > out ';'

dos2unix out 2>/dev/null
dos2unix ../classes.default/out 2>/dev/null
diff out ../classes.default/out

cd ..
