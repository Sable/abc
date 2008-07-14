#!/bin/sh
echo `pwd`
ja-modules.sh -debug -d classes -instance-modules M1 *.ast *.jrag *.java *.module 2>&1 > out
diff out out.default
