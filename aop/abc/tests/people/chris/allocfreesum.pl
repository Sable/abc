#!/usr/bin/perl

# Script summarise output of allocFree aspect
# Will output a line for each creation site that
# has varying numbers of live objects after each run.
# In each line, a number is displayed each time the number
# of live objects is different to that of the previous cycle.


MAIN:while(<> !~/after reset/)
{

#skip
}
my %lines;
while(my $l = <>)
{
  if ($l =~/^(.*)\|.* => \( total: (\d+) , live: (\d+)\)$/)
  {
     
     my $k = $1;
     
     $lines{$k} = [] unless ref $lines{$k};
     push @{$lines{$k}}, $3;
  }

}

foreach my $k (keys %lines)
{
   $loc = $k;
   $values = (join ", ", @{$lines{$k}});
  write;
}


format STDOUT =
  @<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< @<<<<<<<<<<<<<<<<<<<<
$loc, $values
.
