#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module org.x.y.parserapplication *.module *.java pack/*.java ../testlib/*.module  2>&1 > out
../testlib/posttestclasses.sh
