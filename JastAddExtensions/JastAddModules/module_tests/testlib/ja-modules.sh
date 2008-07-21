#!/bin/sh
java -enableassertions -cp `cygpath -w ~/user/jaworkspace/JastAddModules/classes` jastadd.JastAddModules "$@"
