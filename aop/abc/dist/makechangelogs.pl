#!/usr/bin/perl -w

use CGI qw(&escapeHTML);

my @entries=();

my $currentitem;

my $expectingauthor;
my $currentauthor=\$expectingauthor;
my $restriction='';

my @days=('Sun','Mon','Tue','Wed','Thu','Fri','Sat');
my @months=('Jan','Feb','Mar','Apr','May','Jun',
            'Jul','Aug','Sep','Oct','Nov','Dec');

my $mode='';

while(<STDIN>) {
  next unless /\S/; # ignore blank lines
  next if /^#/;     # and comments
  if(/^&(.*)$/) {
    $$currentauthor=$1;
    my $expectingauthor;
    $currentauthor=\$expectingauthor;
  } elsif(/^%([\w\.]+)\s+(\d+)$/) {
    push @entries,$currentitem if defined $currentitem;
    $$currentauthor='' if !defined $$currentauthor;
    $currentitem={ version => $1, 
                   date => $2,
                   author => $currentauthor,
                   changes => [] };
  } elsif(/^\$(.*)$/) {
    die 'Must have a version before an entry' unless defined $currentitem;
    if($mode eq 'ignorenext') {
      $mode='ignoring';
      next;
    } elsif($mode eq 'ignoring') {
      $mode='';
    }
    my $change={text => [$1]};
    $change->{'restriction'}=$restriction;
    push @{$currentitem->{'changes'}},$change;
    $restriction='';
  } elsif(/^\;(.*)$/) {
    die 'Must have an entry before a continuation line' 
      unless defined $currentitem;
    next if $mode eq 'ignoring';
    my $currententries=$currentitem->{'changes'};
    die 'Must have an entry before a continuation line'
      unless @$currententries;
    push @{$currententries->[$#$currententries]->{'text'}},$1;
  } elsif(/^\^(\w+):/) {
    $restriction=$1;
    next if defined $ARGV[0] 
      && ($1 eq $ARGV[0] || $ARGV[0] eq 'latest' || $ARGV[0] eq 'html');
    $mode='ignorenext';
  }
}
push @entries,$currentitem if defined $currentitem;
die 'Must end with an author entry' if defined $$currentauthor;

if(defined $ARGV[0] && $ARGV[0] eq 'redhat') {
  foreach my $entry (@entries) {
    my @dateinfo=gmtime($entry->{'date'});
    print '* '.$days[$dateinfo[6]].' '.$months[$dateinfo[4]]
              .' '.$dateinfo[3].' '.(1900+$dateinfo[5]).' '
              .${$entry->{'author'}}.' '
              .$entry->{'version'}.'-1'
              ."\n";
    foreach my $changes (@{$entry->{'changes'}}) {
      print '- '.$changes->{'text'}->[0]."\n";
      shift @{$changes->{'text'}};
      foreach my $item (@{$changes->{'text'}}) {
	print "  $item\n";
      }
    }
    print "\n";
  }
} elsif(defined $ARGV[0] && $ARGV[0] eq 'debian') {
  foreach my $entry (@entries) {
    print "photopub (".$entry->{'version'}.") unstable; urgency=low\n\n";
    foreach my $changes (@{$entry->{'changes'}}) {
      print '  * '.$changes->{'text'}->[0]."\n";
      shift @{$changes->{'text'}};
      foreach my $item (@{$changes->{'text'}}) {
	print "    $item\n";
      }
    }
    print "\n";
    my @dateinfo=gmtime($entry->{'date'});
    print ' -- '.${$entry->{'author'}}.'  '
                .$days[$dateinfo[6]].', '.$dateinfo[3].' '
                .$months[$dateinfo[4]].' '.(1900+$dateinfo[5]).' '
                .&pad($dateinfo[2]).':'.&pad($dateinfo[1]).':'
                .&pad($dateinfo[0])." +0000\n\n";
  }
} elsif(defined $ARGV[0] && $ARGV[0] eq 'latest') {
  die 'No entries at all!' unless defined $entries[0];
  foreach my $changes (@{$entries[0]->{'changes'}}) {
    print '- '.$changes->{'text'}->[0]."\n";
    shift @{$changes->{'text'}};
    foreach my $item (@{$changes->{'text'}}) {
      print "  $item\n";
    }
  }
  print "\n";
} elsif(defined $ARGV[0] && $ARGV[0] eq 'html') {
  print << 'ENDHEADER';
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.1//EN"
    "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
ENDHEADER
  print "<head><title>abc changelog</title></head>\n";
  print "<body>";
  foreach my $entry (@entries) {
    print "<h2>Version ".&escapeHTML($entry->{'version'})."</h2>\n";
    my @dateinfo=gmtime($entry->{'date'});
    print "<h3>".$days[$dateinfo[6]].', '.$dateinfo[3].' '
                .$months[$dateinfo[4]].' '.(1900+$dateinfo[5]).' '
                .&pad($dateinfo[2]).':'.&pad($dateinfo[1]).':'
                .&pad($dateinfo[0])." +0000</h3>\n";
    print "<ul>\n";
    foreach my $changes (@{$entry->{'changes'}}) {
      print "<li>\n";
      if($changes->{'restriction'} eq 'debian') {
	print "Debian package: ";
      } elsif($changes->{'restriction'} eq 'redhat') {
	print "Redhat package: ";
      }
      foreach my $item (@{$changes->{'text'}}) {
	print &escapeHTML($item)."\n";
      }
      print "</li>\n";
    }
    print "</ul>\n";
    print "<address>".&escapeHTML(${$entry->{'author'}})."</address>\n";
    print "<hr />\n";
  }
  print "</body>\n";
  print "</html>\n";
} else {
  die "Usage: $0 [redhat|debian|latest|html]";
}

sub pad {
  return sprintf('%02d',shift);
}
