#!/bin/sh
echo `pwd`
ja-modules.sh -debug -d classes -instance-modules m1 *.java *.module *.ast *.jrag 2>&1 > out
#diff out out.default
#diff -r classes classes.default
