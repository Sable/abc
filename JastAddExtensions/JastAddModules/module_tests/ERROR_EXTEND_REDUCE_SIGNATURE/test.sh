#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module m1xx *.module *.java ../testlib/*.module  2>&1 > out
../testlib/posttest.sh
