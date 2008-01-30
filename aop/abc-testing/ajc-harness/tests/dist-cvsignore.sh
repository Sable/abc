#!/bin/sh

# Run this from the tests directory as ./dist-cvsignore.sh

find . -name .cvsignore ! -path ./.cvsignore -exec cp base/.cvsignore {} \;
