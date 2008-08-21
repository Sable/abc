#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -jastaddframework  -d classes -instance-module "supermodule" *.java *.module ../testlib/*.module 2>&1 > out
../testlib/posttesterr.sh
