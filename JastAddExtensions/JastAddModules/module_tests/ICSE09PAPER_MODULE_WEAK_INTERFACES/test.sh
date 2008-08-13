#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module myapplication *.module *.java 2>&1 > out
../testlib/posttestclasses.sh
