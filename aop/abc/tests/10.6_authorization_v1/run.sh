#!/bin/sh
java -cp dest:$CLASSPATH -Djava.security.auth.login.config=./sample_jaas.config -Djava.security.policy=./security.policy banking.Test
