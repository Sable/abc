#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module M1 *.ast *.jrag *.java *.module ../testlib/*.java ../testlib/*.module 2>&1 > out
../testlib/posttest.sh
