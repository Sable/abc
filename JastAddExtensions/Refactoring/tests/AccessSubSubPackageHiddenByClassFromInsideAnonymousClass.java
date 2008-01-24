package tests;

import AST.FileRange;

public class AccessSubSubPackageHiddenByClassFromInsideAnonymousClass extends AccessPackage {

	public AccessSubSubPackageHiddenByClassFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test14.pkg1", 
				new FileRange("Access/test14/Test.java", 15, 13, 15, 23),
				null);
	}

}
