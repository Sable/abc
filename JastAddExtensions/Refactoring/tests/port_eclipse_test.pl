#!/usr/bin/perl

$BASEDIR = "/home/maxs/workspace/org.eclipse.jdt.ui.tests.refactoring/resources";
#$ECLIPSE_TESTKIND = "RenameNonPrivateField";
$ECLIPSE_TESTKIND = "RenameType";
#$OUR_TESTKIND = "RenameField";
$OUR_TESTKIND = "RenameType";
#$JAVAPROG = "main.RunFieldRenameTests";
$JAVAPROG = "main.RunTypeRenameTests";

$testno = $ARGV[0];
$ourtestno = $testno+66;
$testdir = "$BASEDIR/$ECLIPSE_TESTKIND/testFail$testno/in";
$infile = "$testdir/A.java";
$newdir = "$OUR_TESTKIND/test$ourtestno";
$newindir = "$newdir/in";
$newinfile = "$newindir/A.java";
#$newoutdir = "$newdir/out";
#$newoutfile = "$newoutdir/A.java";

open(INFILE, "$infile") or die "No such file: $infile\n";
$tst = join('', <INFILE>);
close(INFILE);

system("mkdir -p $newindir") and die "Cannot make directory: $newindir\n";
open(NEWFILE, ">$newinfile") or die "Cannot open: $newinfile.\n";
#print NEWFILE "// $newinfile p A f g\n";
print NEWFILE "// $newinfile p Outer.Inner.A B\n";
print NEWFILE $tst;
close(NEWFILE);

open(JAVA, "java -cp .. $JAVAPROG test$ourtestno |") or die "Cannot start java.\n";
@out = <JAVA>;
close(JAVA);

print "Input:\n$tst\n";
#print "Does this output look OK?\n".join('', @out)."\n";
die ("Output:\n".join('', @out)."\n");

$ans = <STDIN>;
if($ans =~ /^y/) {
    system("mkdir -p $newoutdir") and die "Cannot make directory: $newoutdir\n";
    $no_outfile_yet = 1;
    for($i=0;$i<=$#out;++$i) {
	$_ = $out[$i];
	if(/^>>>>(.*)<<<<$/) {
	    $file = $1;
	    $file =~ s/$newindir/$newoutdir/;
	    $no_outfile_yet = 0;
	    close(NEWFILE) unless $no_outfile_yet;
	    open(NEWFILE, ">$file") or die "Cannot open: $file.\n";
	} else {
	    if($no_outfile_yet) {
		open(NEWFILE, ">$newoutfile") 
		    or die "Cannot open: $newoutfile.\n";
		$no_outfile_yet = 0;
	    }
	    print NEWFILE $_;
	}
    }
    close(NEWFILE) unless $no_outfile_yet;
}
