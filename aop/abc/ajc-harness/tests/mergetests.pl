#!/usr/bin/perl
# Copies lines between the start pattern <!-- start $srcfile --> and the
# end pattern <!-- end $srcfile --> from the source file to the destfile and
# prints the result to stdout.
#
# Can be useful for integrating extension test files with the main test file.
# Would have been much better implemented if it actually parsed the XML.

use warnings;
use strict;

if ($#ARGV != 1) {
	print 
"Merges the tests in sourcefile with the tests in the destination file

Usage: 
	mergetests <sourcefile> <destfile>
";
	exit(0);
}

my $filename1 = $ARGV[0];
my $filename2 = $ARGV[1];

my $SRCFILE;
my $DESTFILE;

open SRCFILE, $filename1 || die("Can't open $filename1");
open DESTFILE, $filename2 || die("Can't open $filename2");

my $startpattern = "<!--\\s*start\\s$filename1.*>.*";
my $endpattern = "<!--\\s*end\\s$filename1.*>.*";

my $srcline;
my $destline;

DEST1: while ($destline = <DESTFILE>) {
	print $destline;
	last DEST1 if $destline =~$startpattern;
}

SRC1: while ($srcline = <SRCFILE>) {
	last SRC1 if $srcline =~ $startpattern;
}

SRC2: while ($srcline = <SRCFILE>) {
	print $srcline;
	last SRC2 if $srcline =~ $endpattern;
}

DEST2: while ($destline = <DESTFILE>) {
	last DEST2 if $destline =~ $endpattern;
}

while ($destline = <DESTFILE>) {
	print $destline;
}
