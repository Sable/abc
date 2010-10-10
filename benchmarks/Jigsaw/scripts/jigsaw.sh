#!/bin/sh
# Jigsaw
# $Id$
# Jigsaw launcher example

# Adding Jigsaw into your class path (using team version):
JIGSAW_HOME=`pwd | sed 's/scripts//'` 
export JIGSAW_HOME

LD_LIBRARY_PATH=${JIGSAW_HOME}/lib
export LD_LIBRARY_PATH

CLASSPATH=${JIGSAW_HOME}/classes/jigsaw.jar:${JIGSAW_HOME}/classes/sax.jar:${JIGSAW_HOME}/classes/xp.jar:${JIGSAW_HOME}/classes/servlet.jar:${JIGSAW_HOME}/classes/xerces.jar:${JIGSAW_HOME}/classes/Tidy.jar
export CLASSPATH

java -Xms16m -Xmx128m org.w3c.jigsaw.Main -root ${JIGSAW_HOME}/Jigsaw $* 
# done.
