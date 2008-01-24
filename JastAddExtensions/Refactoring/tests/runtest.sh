#!/bin/sh

cd data
java -cp ../..:../../tools/junit.jar junit.textui.TestRunner tests.$1

