#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module m1 *.module *.java m1/*.java ../testlib/*.module ./pack/*.java ./m3/*.java 2>&1 > out
../testlib/posttestclasses.sh
