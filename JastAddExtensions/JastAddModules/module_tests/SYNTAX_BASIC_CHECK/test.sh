#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -debug -d classes -instance-module M *.ast *.jrag *.java *.module ../testlib/*.java ../testlib/*.module 2>&1 > out
../testlib/posttest.sh
