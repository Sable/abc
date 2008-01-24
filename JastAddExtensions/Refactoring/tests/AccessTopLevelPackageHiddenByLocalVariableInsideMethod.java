package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByLocalVariableInsideMethod extends AccessPackage {

	public AccessTopLevelPackageHiddenByLocalVariableInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test10/Test.java", 13, 9, 13, 18),
				null);
	}

}
