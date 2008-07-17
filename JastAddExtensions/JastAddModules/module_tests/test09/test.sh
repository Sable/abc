#!/bin/sh
echo `pwd`
rm classes/* -r -f
ja-modules.sh -debug -d classes -instance-module M1 *.java *.module 2>&1 > out
diff out out.default
