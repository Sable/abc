#!/bin/sh
#../testlib/pretest.sh
echo `pwd`
../testlib/ja-modules.sh -jastaddframework  -debug -instance-module m1 -d classes -classpath "./classlib/classes;./classlib/jar/classes/jar.jar" *.java *.module ../testlib/*.module 2>&1 > out
../testlib/posttestclassesclasspath.sh "../classlib/classes;../classlib/jar/classes/jar.jar"
