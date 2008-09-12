#!/usr/bin/perl
my $TEST_MIN = 1;
my $TEST_MAX = 100;
if ($#ARGV > -1) {
	$TEST_MIN = $ARGV[0];
	$TEST_MAX = $ARGV[0];
	
}
my $JASTADD_MODULES_BASEDIR = "../../";
my $TESTDIR = ".";
my $TESTLIBDIR = "../testlib/";

sub trim($)
{
	my $string = shift;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}


#clean class, out files
system "rm -r $TESTDIR/m1 -f";
system "rm -r $TESTDIR/jastadd\\\$framework -f";
system "rm $TESTDIR/*.out -f";

#run tests
for (my $i = $TEST_MIN; $i <= $TEST_MAX; $i++) {
	my $test_name = "Test$i";
	my $test_file = "$TESTDIR/$test_name.java";

	#check if test exists
	open JAVAFILE, "$test_file" or next;

	#compile with jastadd
	open OPTIONS , "$TESTDIR/$test_name.options";
	my $options = "";
	while (my $line = <OPTIONS>) {
		$options .= " " . trim($line);
	}

	my $files = "$test_file ";
	$files .= "$TESTDIR/m1.module ";
	$files .= "$TESTDIR/package-info.java ";
	$files .= "$TESTLIBDIR/ASTNode.java ";
	$files .= "$TESTLIBDIR/ASTNode\\\$State.java ";
	$files .= "$TESTLIBDIR/List.java ";
	$files .= "$TESTLIBDIR/Opt.java ";
	$files .= "$TESTLIBDIR/jastadd\\\$framework.module ";


	my $cmdline = "java -cp \"$JASTADD_MODULES_BASEDIR/classes;.\" jastadd.JastAddModules -d $TESTDIR -jastaddframework -instance-module m1 $options $files";
	print "$test_name\n";
	#print "JastAdd command: $cmdline\n";
	system $cmdline;
	
	#run generated file
	my $result_file = "$TESTDIR/$test_name.out";
	my $correct_file = "$TESTDIR/$test_name.result";
	my $javacmdline = "java -cp \"$JASTADD_MODULES_BASEDIR/classes;$TESTDIR\" m1.\\\$test.$test_name 2>&1 > $result_file";
	#print "Java command: $javacmdline\n";
	system $javacmdline;

	#diff results
	system "dos2unix $result_file 2>/dev/null";
	system "dos2unix $correct_file 2>/dev/null";
	system "diff --ignore-space-change $result_file $correct_file | tee $test_name.diff";
}
