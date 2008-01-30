#!/usr/bin/perl -w

if(scalar @ARGV!=2) {
  print STDERR "Usage: $0 <version> <unixtime>\n";
  exit(1);
}

while(<STDIN>) {
  if((!/\S/) || /^#/) {
    print;
  } else {
    last;
  }
}

print '%'.$ARGV[0].' '.$ARGV[1]."\n";
print if $_;

while(<STDIN>) {
  print;
}

