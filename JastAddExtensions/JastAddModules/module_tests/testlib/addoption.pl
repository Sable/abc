#!/usr/bin/perl
my $option = $ARGV[0];
my $file = $ARGV[1];
print "Adding option: " . $option . " to file: " . $file . "\n";

open INFILE, "< $file" or die "Can't find file $file";
open TEMPFILE, "> $file" . ".temp" or die "Can't open temporary file";

while (my $line = <INFILE>) {
	if ($line =~ /(.*ja-modules\.sh)(.*)/) {
		my $left = $1;
		my $right = $2;
		print TEMPFILE $left . " $option " . $right ."\n";
	} else {
		print TEMPFILE $line;
	}
}
