#!/usr/bin/perl -w

use strict;

my $argc = @ARGV;
if ($argc<1) {
  print 
"Usage: run_cases.pl XMLFILE [dir-filter [title-filter]]
Runs the cases listed in XMLFILE individually. 
Outputs passed.xml and failed.xml.
" ;
exit(0);
}

my $dirfilter="";
if ($argc>1) {
  $dirfilter=$ARGV[1];
}

my $titlefilter="";
if ($argc>2) {
  $titlefilter=$ARGV[2];
}

my $failed=0;
my $succeeded=0; #sanity


my $xmlprefix="<!DOCTYPE suite SYSTEM \"../tests/ajcTestSuite.dtd\"> \n <suite> \n ";
my $xmlsuffix="</suite> \n";

system("rm -f failed.output");

open(FAILED, "> failed.xml") or die "cannot open failed.xml";
open(PASSED, "> passed.xml") or die "cannot open passed.xml";
print FAILED $xmlprefix;
print PASSED $xmlprefix;


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
     system("mv tmp.output $dir/$filename");
     system("echo '*.output' > $dir/.cvsignore");
     $failed++;
     print FAILED $xmlpart;
   } elsif ($out =~ m/\nPASS/gs) {
     print "Passed. ";
     system("rm -f $dir/$filename");
     $succeeded++;
     print PASSED $xmlpart;
   } else {
     system("echo $title >> cases_with_invalid_output.txt");
     $count--;
     #die "could not find FAIL or PASS\n";
     $countinvalid++;
   }
   print "Current status: $failed failed, $succeeded passed.\n";
}

open(INPUT, "< $ARGV[0]") || die "can't open input file"; 
my $file;
while (<INPUT>) {
  $file .= $_;
}

$file =~ s/<ajc-test[^>]+dir=\"([^\"]*)\"[^>]+title=\"([^\"]*)\"[^>]*>.*?<\/ajc-test>/do_case($1,$2,$&)/sge;

print "Tests: $count\nFailed: $failed\nPassed: $succeeded\n";

if ($countinvalid > 0 ) {
  print "Tests with invalid output: $countinvalid.\n";
}

print FAILED $xmlsuffix;
print PASSED $xmlsuffix;

$succeeded+$failed==$count || die "i can't count!\n";



# s/<ajc-test[^>]+dir=\"([^\"]*)\"[^>]+title=\"([^\"]*)\"[^>]*>.*?<\/ajc-test>/do_case($1,$2,$&)/sge
