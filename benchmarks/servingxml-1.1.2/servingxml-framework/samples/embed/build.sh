#!/bin/sh
# build.sh -- Build Script for the "ServingXML" Application
#

echo "servingxml sample web app Build"
echo "----------------------------------"

# set the target = to either 1st arg or default (dist)
if [ ! -n "$1" ] ; then
   TARGET=dist
else                                   
   TARGET=$1   
fi

echo "TARGET is: " $TARGET

# check for and set JAVA_HOME
if [ "$JAVA_HOME" = "" ] ; then
    echo "Warning: JAVA_HOME environment variable is not set."
    exit
fi
if [ "$ANT_HOME" = "" ] ; then
    echo "Warning: ANT_HOME environment variable is not set."
    exit
fi

echo "JAVA_HOME is: " $JAVA_HOME
echo "ANT_HOME is: " $ANT_HOME

$ANT_HOME/bin/ant -buildfile build.xml $TARGET

