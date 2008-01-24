package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByLocalClass extends AccessPackage {

	public AccessTopLevelPackageHiddenByLocalClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test7/Test.java", 12, 5, 12, 16),
				null);
	}

}
