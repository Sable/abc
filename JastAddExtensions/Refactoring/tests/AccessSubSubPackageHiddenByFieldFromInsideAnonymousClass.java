package tests;

import AST.FileRange;

public class AccessSubSubPackageHiddenByFieldFromInsideAnonymousClass extends AccessPackage {

	public AccessSubSubPackageHiddenByFieldFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test13.pkg1", 
				new FileRange("Access/test13/Test.java", 16, 13, 16, 23),
				null);
	}

}
