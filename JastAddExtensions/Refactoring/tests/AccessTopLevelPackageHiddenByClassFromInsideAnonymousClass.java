package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByClassFromInsideAnonymousClass extends AccessPackage {

	public AccessTopLevelPackageHiddenByClassFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test14/Test.java", 15, 13, 15, 23),
				null);
	}

}
