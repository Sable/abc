#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework -d classes -instance-module M1 *.java *.module 2>&1 > out
../testlib/posttesterr.sh
