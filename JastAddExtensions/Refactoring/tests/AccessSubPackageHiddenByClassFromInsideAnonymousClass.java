package tests;

import AST.FileRange;

public class AccessSubPackageHiddenByClassFromInsideAnonymousClass extends AccessPackage {

	public AccessSubPackageHiddenByClassFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test14", 
				new FileRange("Access/test14/Test.java", 15, 13, 15, 23),
				null);
	}

}
