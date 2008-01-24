package tests;

import AST.FileRange;

public class AccessSubPackageHiddenByFieldFromInsideAnonymousClass extends AccessPackage {

	public AccessSubPackageHiddenByFieldFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test13", 
				new FileRange("Access/test13/Test.java", 16, 13, 16, 23),
				null);
	}

}
