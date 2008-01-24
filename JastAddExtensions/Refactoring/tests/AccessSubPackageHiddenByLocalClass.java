package tests;

import AST.FileRange;

public class AccessSubPackageHiddenByLocalClass extends AccessPackage {

	public AccessSubPackageHiddenByLocalClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test7", 
				new FileRange("Access/test7/Test.java", 12, 5, 12, 16),
				null);
	}

}
