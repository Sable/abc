#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module m1 *.module *.java m1pack/*.java m2pack/*.java ../testlib/*.module  2>&1 > out
../testlib/posttestclasses.sh
