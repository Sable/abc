#!/usr/bin/perl
my $TEST_MIN = 1;
my $TEST_MAX = 100;
if ($#ARGV > -1) {
	$TEST_MIN = $ARGV[0];
	$TEST_MAX = $ARGV[0];
	
}
my $JASTADD_BASEDIR = "../";
my $JASTADD_MODULES_BASEDIR = "../";
my $TESTDIR = $JASTADD_MODULES_BASEDIR . "/test";

sub trim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}


#clean class, out files
system "rm $JASTADD_BASEDIR/test/*.class -f";
system "rm $JASTADD_BASEDIR/test/*.out -f";

#run tests
for (my $i = $TEST_MIN; $i <= $TEST_MAX; $i++) {
	my $test_name = "Test$i";
	my $test_file = "$JASTADD_BASEDIR/test/$test_name.java";

	#check if test exists
	open JAVAFILE, "$test_file" or next;

	#compile with jastadd
	open OPTIONS , "$JASTADD_BASEDIR/test/$test_name.options";
	my $options = "";
	while (my $line = <OPTIONS>) {
		$options .= " " . trim($line);
	}

	my $files = "$test_file ";
	$files .= "$JASTADD_BASEDIR/test/ASTNode.java ";
	$files .= "$JASTADD_BASEDIR/test/List.java ";
	$files .= "$JASTADD_BASEDIR/test/Opt.java ";


	my $cmdline = "java -enableassertions -cp \"$JASTADD_MODULES_BASEDIR/classes;.\" jastadd.JastAddModules $options $files";
	print "$test_name\n";
	#print "JastAdd command: $cmdline\n";
	system $cmdline;
	
	#run generated file
	my $result_file = "$TESTDIR/$test_name.out";
	my $correct_file = "$TESTDIR/$test_name.result";
	my $javacmdline = "java -cp \"$JASTADD_MODULES_BASEDIR/classes;$JASTADD_MODULES_BASEDIR\" test.$test_name > $result_file";
	#print "Java command: $javacmdline\n";
	system $javacmdline;

	#diff results
	system "dos2unix $result_file 2>/dev/null";
	system "dos2unix $correct_file 2>/dev/null";
	system "diff --ignore-space-change $result_file $correct_file";
}
