#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module multiversion *.module *.java v1/*.java v2/*.java 2>&1 > out
../testlib/posttestclasses.sh
