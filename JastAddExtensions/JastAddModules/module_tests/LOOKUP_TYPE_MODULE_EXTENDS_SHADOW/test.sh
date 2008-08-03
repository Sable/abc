#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module m1xx *.module *.java ../testlib/*.module m1pack/*.java m1xpack/*.java m1xxpack/*.java 2>&1 > out
../testlib/posttestclasses.sh
