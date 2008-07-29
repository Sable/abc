#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module z *.module *.java ../testlib/*.module  2>&1 > out
../testlib/posttestclasses.sh
