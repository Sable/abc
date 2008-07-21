#!/bin/sh
../testlib/pretest.sh
ja-modules.sh -debug -d classes -instance-module M *.ast *.jrag *.java *.module 2>&1 > out
../testlib/posttest.sh
