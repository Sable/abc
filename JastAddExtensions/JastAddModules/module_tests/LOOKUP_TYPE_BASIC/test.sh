#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module m1 m1.module m2.module m3.module A.java B.java C.java AST.ast AST2.ast AST3.ast Main.java ../testlib/*.module ../testlib/*.java 2>&1 > out
../testlib/posttestclasses.sh
