#!/usr/bin/perl -w

use strict;

print << 'START';
<!-- This file is auto-generated. Changes you make directly will be lost! -->
<!--#set var="title" value="Credits"-->
<!--#set var="file" value="credits.shtml"-->
<!--#set var="logo" value="standard"-->

<!--#include file="includes/header.shtml"-->
<div class="main">
START

my $indashlist=0;
my $inpara=0;

while(<>) {
  s/\</&lt;/g; s/\</&gt;/g;
  s{\babc\b}{<span class=\"abc\">abc</span>}g;
  s{\bajc\b}{<span class=\"abc\">ajc</span>}g;
  s{http://([\w\d\.\/]+)}{<a href=\"http://$1\">http://$1</a>}g;
  if($_=~/^\s+\-(.*)$/) {
    if(!$indashlist) { 
      $indashlist=1;
      $inpara=0;
      print "</p><ul><li>\n"; 
    }
    else { print "</li><li>\n"; }
    print "$1\n";
  } elsif($_=~/^\s+$/) {
    if($indashlist) {
      print "</li></ul>\n";
      $indashlist=0;
    }
    if($inpara) {
      print "</p>\n";
      $inpara=0;
    }
  } else {
    if(!$inpara && !$indashlist) {
      print "<p>\n";
      $inpara=1;
    }
    print $_;
  }
}
if($indashlist) {
  print "</li></ul>\n";
}
if($inpara) {
  print "</p>\n";
}


print << 'END';
</div>

<!--#include file="includes/footer.shtml"-->
END

