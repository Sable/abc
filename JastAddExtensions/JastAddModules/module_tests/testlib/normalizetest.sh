#!/bin/sh
cp out out.default
if [ -d classes ]
	then
		cd classes
		cp -r . ../classes.default
		cd ..
fi
