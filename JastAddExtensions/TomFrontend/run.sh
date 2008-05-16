#!/bin/sh
TOM_HOME=/home/torbjorn/test/JastAddExtensions/Tom/src/dist
for i in "${TOM_HOME}"/lib/runtime/*.jar
  do
    TOM_LIB="$TOM_LIB:$i"
  done
for i in "${TOM_HOME}"/lib/tom/*.jar
  do
    TOM_LIB="$TOM_LIB:$i"
  done
for i in "${TOM_HOME}"/lib/tools/*.jar
  do
    TOM_LIB="$TOM_LIB:$i"
  done
java -classpath $TOM_LIB tom.engine.Tom -X /home/torbjorn/test/JastAddExtensions/Tom/src/config/Tom.xml $@
