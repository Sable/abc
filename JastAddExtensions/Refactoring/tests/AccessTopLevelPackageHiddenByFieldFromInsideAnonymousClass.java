package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByFieldFromInsideAnonymousClass extends AccessPackage {

	public AccessTopLevelPackageHiddenByFieldFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test13/Test.java", 16, 13, 16, 23),
				null);
	}

}
