#!/usr/bin/perl -w

use strict;

my $header='/* abc - The AspectBench Compiler';
my $footer=' */';

my $lgpl=<< 'ENDLGPL';
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
ENDLGPL

my @lines;

{
local $/;
@lines=split("\n",<>);
}

my @copyrights=();
my %copyrightdone=();

shift @lines while scalar @lines>0 && $lines[0]=~/^\s*$/;

if(scalar @lines>0 && $lines[0] eq $header) {
  while($lines[0] ne $footer) {
    if($lines[0]=~/^\s+\*\s+Copyright\s+\([cC]\)\s+([\d\-]+)\s+([\w\s]*\w)/) {
      push @copyrights,{ 'years'=>$1, 'name'=>$2 };
      $copyrightdone{$2}=1;
    }
    shift @lines;
  }
  shift @lines;
  shift @lines while scalar @lines>0 && $lines[0]=~/^\s*$/;
}

my $currentyear=(localtime(time))[5]+1900;

my $package;
my $class;

foreach my $line (@lines) {
  if($line=~/\@author\s+(\w[\w\s]*\w)/ && !exists $copyrightdone{$1}) {
    push @copyrights,{'years'=>"$currentyear",'name'=>$1};
    $copyrightdone{$1}=1;
  }
  if($line=~/^package ([\w\.]+);/) {
    $package=$1;
  }
  if($line=~/class\s+([A-Z]\w*)/ && !defined $class) {
    $class=$1;
  }
  if($line=~/interface\s+([A-Z]\w*)/ && !defined $class) {
    $class=$1;
  }
}

if(!defined $package) {
  die "No package found\n";
}

if(scalar @copyrights==0) {
  print STDERR "Warning: no copyright notice in package $package "
    ."(class $class)\n";
}

print $header."\n";
foreach my $copyright (@copyrights) {
  print " * Copyright (C) ".$copyright->{'years'}." "
    .$copyright->{'name'}."\n";
}
print $lgpl.$footer."\n\n";
foreach my $line (@lines) {
  print "$line\n";
}
