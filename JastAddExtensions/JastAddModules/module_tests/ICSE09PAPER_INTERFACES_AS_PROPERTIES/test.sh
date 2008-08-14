#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module myapplication *.module *.java v1/*.java v2/*.java v3/*.java v4/*.java 2>&1 > out
../testlib/posttestclasses.sh
