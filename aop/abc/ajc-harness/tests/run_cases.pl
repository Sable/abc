#!/usr/bin/perl -w

my $argc = @ARGV;
if ($argc!=1) {
  print 
"Usage: run_cases.pl XMLFILE
Runs the cases listed in XMLFILE individually. 
Outputs passed.xml and failed.xml.
" ;
exit(0);
}

my $file;
my $count=0;
my $failed=0;
my $succeeded=0; #sanity

system("rm -f failed.output");

my $xmlprefix="<!DOCTYPE suite SYSTEM \"../tests/ajcTestSuite.dtd\"> \n <suite> \n ";
my $xmlsuffix="</suite> \n";
open(FAILED, "> failed.xml") or die "cannot open failed.xml";
open(PASSED, "> passed.xml") or die "cannot open passed.xml";
print FAILED $xmlprefix;
print PASSED $xmlprefix;

while (<>) {
  my $xmlpart="";
  my $title;
  my $dir;
  my $inside=0;
  if (m/ajc-test.*dir=\"([^\"]*)\".*title=\"([^\"]*)\"/) {
    $title=$2;
    $dir=$1;
    $xmlpart.=$_;
    $inside=1;
  } elsif (m/ajc-test.*dir=\"([^\"]*)\"/) {
    $inside=1;
    $dir=$1;
    $xmlpart.=$_;
    $_=<>;
    $xmlpart.=$_;
    #print $_;
    while (!m/title=\"([^\"]*)\"/) { 
      $_=<>;
      $xmlpart.=$_;
      m/ajc-test/ && die;
    }
    m/title=\"([^\"]*)\"/;
    $title=$1;
  }
  if ($inside==1) {
    $count++;
   while (!m/\/ajc-test/) { 
      $_=<>;
      $xmlpart.=$_;
      m/ajc-test / && die;
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
   }
   print "Current status: $failed failed, $succeeded passed.\n";
#   print "XML: \n $xmlpart \n\n";
#   $count==2 && die;
  }  
}
print "Tests: $count\nFailed: $failed\nPassed: $succeeded\n";

print FAILED $xmlsuffix;
print PASSED $xmlsuffix;

$succeeded+$failed==$count || die "i can't count!\n";
