#!/bin/sh
echo `pwd`
rm classes/* -r -f
ja-modules.sh -debug -d classes -instance-module m1 m1.module m2.module A.java ASTNode.java List.java Opt.java AST.ast AST2.ast 2>&1 > out
#diff out out.default
#diff -r classes classes.default
