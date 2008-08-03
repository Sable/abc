#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module M1 *.java *.module 2>&1 > out
../testlib/posttest.sh
