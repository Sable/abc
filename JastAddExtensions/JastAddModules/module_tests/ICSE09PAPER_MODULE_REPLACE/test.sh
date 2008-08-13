#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module myapplication *.module *.java v11/*.java v12/*.java v2/*.java 2>&1 > out
../testlib/posttestclasses.sh
