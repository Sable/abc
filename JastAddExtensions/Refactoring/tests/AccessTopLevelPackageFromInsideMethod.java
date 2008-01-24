package tests;

import AST.FileRange;

public class AccessTopLevelPackageFromInsideMethod extends AccessPackage {

	public AccessTopLevelPackageFromInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test8/Test.java", 13, 9, 13, 14),
				"Access");
	}

}
