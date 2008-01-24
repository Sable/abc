package tests;

import AST.FileRange;

public class AccessTopLevelPackageFromInsideAnonymousClass extends AccessPackage {

	public AccessTopLevelPackageFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test11/Test.java", 14, 13, 14, 21),
				"Access");
	}

}
