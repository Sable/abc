#!/bin/sh
cat ../argsheader.in > args.in
find -name "*.java" >> args.in
find -name "*.module" >> args.in
