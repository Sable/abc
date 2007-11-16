#!/bin/sh

java -cp AST:tools/beaver-rt.jar:. JavaChecker $1 
dot -Tps dot/method0.dot -o dot/method0.ps
gv dot/method0.ps
