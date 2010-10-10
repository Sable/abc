#!/bin/sh

CLASSPATH=../classes/jigsaw.jar:../classes/sax.jar:../classes/xp.jar:../classes/servlet.jar:.
export CLASSPATH
cd Jigsaw
java Install
cd ..

