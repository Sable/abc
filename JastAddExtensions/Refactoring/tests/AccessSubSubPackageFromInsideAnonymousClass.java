package tests;

import AST.FileRange;

public class AccessSubSubPackageFromInsideAnonymousClass extends AccessPackage {

	public AccessSubSubPackageFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test11.pkg1", 
				new FileRange("Access/test11/Test.java", 14, 13, 14, 21),
				"Access.test11.pkg1");
	}

}
