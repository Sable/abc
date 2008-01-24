package tests;

import AST.FileRange;

public class AccessSubPackageHiddenByField extends AccessPackage {

	public AccessSubPackageHiddenByField(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test6", 
				new FileRange("Access/test6/Test.java", 11, 5, 11, 14),
				null);
	}

}
