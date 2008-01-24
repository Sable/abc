package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByParameterFromInsideMethod extends AccessPackage {

	public AccessTopLevelPackageHiddenByParameterFromInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test9/Test.java", 12, 9, 12, 18),
				null);
	}

}
