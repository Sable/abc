#!/usr/bin/perl -w

use strict;
use Benchmark;
#

my $argc = @ARGV;
if ($argc<1) {
  print 
"Usage: run_cases.pl [-list [-xml]] [-timeajc [-cutoff SECONDS]] XMLFILE [DIR-FILTER [TITLE-FILTER]] 
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
my $timeajc=0;
my $cutoff=0;
if ($ARGV[$arg] eq "-timeajc") {
	$timeajc=1;
	$arg++;	
	
	if ($ARGV[$arg] eq "-cutoff") {
		$arg++;
		$cutoff=$ARGV[$arg];
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
	open(TIMES, "> times.txt") or die "cannot open times.txt";
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

  
  my $t1total;
  my $t2total;
  if ($timeajc) {
#    ($real, $user, $system, $children_user, $children_system, $iters)
#    my $t1=timeit(1, 'system("./testHarness tmp.xml > tmp.output")');
    my $t1=timeit(1, 'system("java org.aspectj.testing.Harness -logMinAll tmp.xml > tmp.output")');

    $t1total=$t1->[0]; #$t1->[1] + $t1->[2] + $t1->[3] + $t1->[4];
    my $t2=timeit(1, 'system("java -jar ../lib/ajc-testing-harness.jar tmp.xml")');
    $t2total=$t2->[0]; #$t2->[1] + $t2->[2] + $t2->[3] + $t2->[4];
   

  } else {
    system("./testHarness tmp.xml > tmp.output");
  }

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
     if ($timeajc) {
       if ($t1total>$cutoff) {
	 if ($t1total==0.0 || $t2total==0.0) {
	   print TIMES "error ";
	 } else {
	   my $ratio=$t1total/$t2total;
	   printf TIMES "%9.2f ", $ratio;
	 }
	 printf TIMES "abc:%9.2f  ajc:%9.2f  ", $t1total, $t2total;
	 print TIMES " '$title'\n";
	 #printf TIMES "  abc: " . timestr($t1) . " ajc: " . timestr($t2) . " \n";
       }
     }
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

$file =~ s/<!--.*?-->//sg;
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

	close TIMES;
	
	system("cat times.txt | sort > tmptimes.txt; cp tmptimes.txt times.txt");
}

# s/<ajc-test[^>]+dir=\"([^\"]*)\"[^>]+title=\"([^\"]*)\"[^>]*>.*?<\/ajc-test>/do_case($1,$2,$&)/sge
