package tests;

import AST.FileRange;

public class AccessSubPackageFromInsideAnonymousClass extends AccessPackage {

	public AccessSubPackageFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test11", 
				new FileRange("Access/test11/Test.java", 14, 13, 14, 21),
				"Access.test11");
	}

}
