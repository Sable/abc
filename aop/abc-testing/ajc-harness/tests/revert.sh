#!/bin/sh -e

FILES="full_current.output failed_current.output \
      failed_current.xml passed_current.xml \
      skipped_current.xml \
      failed_check_current.xml \
      passed_check_current.xml"

cvs update -D$1 $FILES
cvs tag -l temp
cvs update -A $FILES
cvs update -j HEAD -j temp $FILES
cvs tag -l -d temp
