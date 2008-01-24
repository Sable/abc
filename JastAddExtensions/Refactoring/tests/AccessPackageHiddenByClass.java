package tests;

import AST.FileRange;

public class AccessPackageHiddenByClass extends AccessPackage {

	public AccessPackageHiddenByClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test3", 
				new FileRange("Access/test3/Test.java", 11, 5, 11, 20),
				null);
	}

}
