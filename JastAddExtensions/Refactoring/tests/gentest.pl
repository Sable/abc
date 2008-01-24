#!/usr/bin/perl

sub emitTest {
    print "runFieldAccessTest(new FileRange(\"$_[0]\", $_[1], $_[2], $_[3], $_[4]), new FileRange(\"$_[5]\", $_[6], $_[7], $_[8], $_[9]), new $_[10]);\n";
}

$_ = join('', <STDIN>);
s/testLocalVariableAccess\(new FileRange\((\d+), (\d+), (\d+), (\d+)\), new FileRange\((\d+), (\d+), (\d+), (\d+)\),\s* new (.*?),\s*"([^"]*)"\);/emitTest($10, $1, $2, $3, $4, $10, $5, $6, $7, $8, $9)/eg;s/testLocalVariableAccess\(new FileRange\((\d+), (\d+), (\d+), (\d+)\), new FileRange\((\d+), (\d+), (\d+), (\d+)\),\s* null(.*?),\s*"([^"]*)"\);/emitTest($10, $1, $2, $3, $4, $10, $5, $6, $7, $8, $9)/eg;

