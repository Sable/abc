#!/bin/sh
/bin/find -wholename "*ERROR*/test.sh" -execdir "./test.sh" ';'
