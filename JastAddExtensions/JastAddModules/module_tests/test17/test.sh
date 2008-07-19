#!/bin/sh
../testlib/pretest.sh
ja-modules.sh -debug -d classes -instance-module m1 m1.module m2.module m3.module jastadd\$framework.module A.java B.java C.java ASTNode.java List.java Opt.java AST.ast AST2.ast AST3.ast 2>&1 > out
../testlib/posttestclasses.sh
