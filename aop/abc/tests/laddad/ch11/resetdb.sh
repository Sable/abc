#!/bin/sh
mysql -u laddad -pladdad -e"use laddad; truncate table accounts; insert into accounts VALUES (1,0); insert into accounts VALUES (2, 0); insert into accounts VALUES (3,0); "
