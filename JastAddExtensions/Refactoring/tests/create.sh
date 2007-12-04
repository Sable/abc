#!/bin/bash
(rm -rf RenameField/$1/out) &&
(java -cp .. main.RunFieldRenameTests $1 >tmp) &&
(mkdir RenameField/$1/out) &&
(mv tmp RenameField/$1/out/A.java)
