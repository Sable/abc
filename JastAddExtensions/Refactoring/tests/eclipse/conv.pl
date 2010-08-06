#!/usr/bin/perl -i

$s = 0;
while(<>) {
	chomp;
	if(/^\s*assertNotNull\(in\)/) {
		print "$_\n";
		print "		String originalProgram = in.toString();\n";
		print "		in.startRecordingASTChanges();\n";
		$s++;
	} elsif($s == 1 && /^\t\}/) {
		print "		in.undoAll();\n";
		print "		assertEquals(originalProgram, in.toString());\n";
		print "$_\n";
		$s=0;
	} else {
		print "$_\n";
	}
}
