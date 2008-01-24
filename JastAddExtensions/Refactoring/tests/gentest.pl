#!/usr/bin/perl

# Example Usage:
#
#   gentest AccessPackageHiddenByClass Access.test3 Access/test3/Test.java 11 5 11 20 null
#
# This generates a package access test which tries to access package
# Access.test3 from the AST node located at positions (11,5)-(11,20) in file
# Access/test3/Test.java, expecting a null result.

$testname = $ARGV[0];
open(TESTCLASS, ">$testname.java") or die "couldn't open java file\n";
print TESTCLASS <<EOT;
package tests;

import AST.FileRange;

public class $testname extends AccessPackage {

	public $testname(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("$ARGV[1]", 
				new FileRange("$ARGV[2]", $ARGV[3], $ARGV[4], $ARGV[5], $ARGV[6]),
				$ARGV[7]);
	}

}
EOT

close(TESTCLASS);
