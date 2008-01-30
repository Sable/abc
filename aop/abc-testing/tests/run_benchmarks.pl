#! /usr/bin/perl -w

$argc=@ARGV;
if ($argc==0) {
	@benchmarks=('bean', 'gregor_bean', 'gof_adapter', 'gof_bridge', 'nullptr', 'nullptrafter', 
		'productlines', 'telecom', 'figure',  'quicksort', 'gregor_quicksort', 'oege_quicksort', 
                'dcm', 'LoD');
} else {
	@benchmarks=@ARGV;
}
#my $report="";

foreach $dir (@benchmarks) {
	print "Processing $dir\n";
	#"$dir outout comparison:\n";
	if (chdir $dir) {
		system("./abcit > abcc.out 2>abccerr.out");
		$abcout=`cat abccerr.out`;
		$abc=0;
		if ($abcout =~ m/Breakdown of abc phases/) {
			system("./runit > abc.out 2>abcerr.out");
			$abcsize=length `cat abc.out`;
			$abc=1;
		} else {
			print " abc compilation failed\n";
		}
		system("./ajcit > ajcc.out 2> ajccerr.out");
		$ajcout=`cat ajccerr.out`;
		$ajc=0;
		if ((length $ajcout)==0) {
			system("./runit > ajc.out 2>ajcerr.out");
			$ajcsize=length `cat ajc.out`;
			$ajc=1;
		} else {
			print " ajc compilation failed\n";
		}
		if ($abc==1 && $ajc==1) {
			$diff = `diff -u ajc.out abc.out`;
			if ((length $diff)>20000) {
				$diff="diff too long";
			}
			if ((length $diff)>0) {
				print " abc output size: $abcsize\n";
				print " ajc output size: $ajcsize\n";
				print " $diff\n";
			} else {
				print " ok.\n";
			}
		}
		system("rm -f abcc.out abc.out ajcc.out ajc.out abccerr.out ajccerr.out ajcerr.out abcerr.out");
		chdir "..";
	} else {
		print " Could not find $dir\n";
	}
}

#print "Summary: \n";
#print "$report\n";
