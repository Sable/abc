#!/bin/sh
../testlib/pretest.sh
ja-modules.sh -debug -d classes -instance-module M1 *.java *.module 2>&1 > out
../testlib/posttest.sh
