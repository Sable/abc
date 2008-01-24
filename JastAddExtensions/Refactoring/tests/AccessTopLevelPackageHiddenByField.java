package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByField extends AccessPackage {

	public AccessTopLevelPackageHiddenByField(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test6/Test.java", 11, 5, 11, 14),
				null);
	}

}
