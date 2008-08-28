#!/bin/sh
/bin/find -wholename "*INTERFACE*/test.sh" -execdir "./test.sh" ';'
