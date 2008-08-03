#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module com.xyz.m1 *.module *.ast *.java ../testlib/*.module ../testlib/*.java 2>&1 > out
../testlib/posttestclasses.sh
