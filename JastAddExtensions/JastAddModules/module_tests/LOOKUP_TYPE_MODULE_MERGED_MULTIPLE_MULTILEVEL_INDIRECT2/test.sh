#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module m0 *.java *.module 2>&1 > out
../testlib/posttestclasses.sh
