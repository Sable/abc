#!/bin/sh
../testlib/pretest.sh
ja-modules.sh -debug -d classes -instance-module m1 *.module *.java 2>&1 > out
../testlib/posttestclasses.sh
