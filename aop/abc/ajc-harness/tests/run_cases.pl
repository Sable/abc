#!/usr/bin/perl -w

use strict;

my $argc = @ARGV;
if ($argc<1) {
  print 
"Usage: run_cases.pl [-list [-xml]] XMLFILE [DIR-FILTER [TITLE-FILTER]] 
Runs the cases listed in XMLFILE individually. 
Outputs passed.xml and failed.xml.
" ;
exit(0);
}

my $arg=0;

my $skiptests=0;
my $listxml=0;
if ($ARGV[$arg] eq "-list") {
	$skiptests=1;
	$arg++;
	
	if ($ARGV[$arg] eq "-xml") {
		$listxml=1;
		$arg++;
	}
	
}

my $inputfile;
if ($argc>$arg) {
 	$inputfile=$ARGV[$arg];
} else {
	die "must specify input file";
}
$arg++;

my $dirfilter="";
if ($argc>$arg) {
  $dirfilter=$ARGV[$arg];
}
$arg++;

my $titlefilter="";
if ($argc>$arg) {
  $titlefilter=$ARGV[$arg];
}
$arg++;



my $failed=0;
my $succeeded=0; #sanity


my $xmlprefix="<!DOCTYPE suite SYSTEM \"../tests/ajcTestSuite.dtd\"> \n <suite> \n ";
my $xmlsuffix="</suite> \n";

if ($skiptests) {

} else {
	system("rm -f failed.output");
	
	open(FAILED, "> failed.xml") or die "cannot open failed.xml";
	open(PASSED, "> passed.xml") or die "cannot open passed.xml";
	print FAILED $xmlprefix;
	print PASSED $xmlprefix;
}

my $count=0;
my $countinvalid=0;

sub do_case {
  my $dir=$_[0];
  my $title=$_[1];
  my $xmlpart=$_[2];

  if ((length $dirfilter) > 0) {
    if ($dir !~ m/$dirfilter/) {
      #print "skipping case (dir-filter): $dir: $title\n";
      return "";
    }
  }
  if ((length $titlefilter) > 0) {
    if ($title !~ m/$titlefilter/) {
      #print "skipping case (title-filter): $dir: $title\n";
      return "";
    }
  }

  $count++;
#  print "dir: $dir\ntitle: $title\nXML: $xmlpart\n";

	if ($skiptests) {
		print "Case $count ($dir): $title\n";
		if ($listxml) {
			print "$xmlpart\n";
		}
		return "";
	}
	

   open(TMP, "> tmp.xml") || die;
   print TMP "$xmlprefix $xmlpart $xmlsuffix";
   close TMP;
   print "Executing test $count ($dir): $title\n";
   system("./testHarness tmp.xml > tmp.output");

   my $filename=$title;
   $filename =~ s/([^a-zA-Z0-9])/_/g;
   $filename .= ".output";

   my $out=`cat tmp.output`;
   if ($out =~ m/\nFAIL/gs) {
     print "Failed. ";  
     system("cat tmp.output >> failed.output");
     system("cat tmp.xml >> tmp.output");
     system("mv tmp.output $dir/$filename");
     system("echo '*.output' > $dir/.cvsignore");
     $failed++;
     print FAILED "$xmlpart\n";
   } elsif ($out =~ m/\nPASS/gs) {
     print "Passed. ";
     system("rm -f $dir/$filename");
     $succeeded++;
     print PASSED "$xmlpart\n";
   } else {
     system("echo $title >> cases_with_invalid_output.txt");
     $count--;
     #die "could not find FAIL or PASS\n";
     $countinvalid++;
   }
   print "Current status: $failed failed, $succeeded passed.\n";
}

open(INPUT, "< $inputfile") || die "can't open input file"; 
my $file;
while (<INPUT>) {
  $file .= $_;
}
if (!$file) {
	die "invalid input file '$inputfile'";
}

$file =~ s/<ajc-test[^>]+dir=\"([^\"]*)\"[^>]+title=\"([^\"]*)\"[^>]*>.*?<\/ajc-test>/do_case($1,$2,$&)/sge;

if ($skiptests) {

} else {
	print "Tests: $count\nFailed: $failed\nPassed: $succeeded\n";
	
	if ($countinvalid > 0 ) {
	  print "Tests with invalid output: $countinvalid.\n";
	}
	
	print FAILED $xmlsuffix;
	print PASSED $xmlsuffix;
	
	$succeeded+$failed==$count || die "i can't count!\n";
	
}

# s/<ajc-test[^>]+dir=\"([^\"]*)\"[^>]+title=\"([^\"]*)\"[^>]*>.*?<\/ajc-test>/do_case($1,$2,$&)/sge
