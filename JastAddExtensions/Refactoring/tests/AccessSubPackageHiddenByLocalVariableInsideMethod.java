package tests;

import AST.FileRange;

public class AccessSubPackageHiddenByLocalVariableInsideMethod extends AccessPackage {

	public AccessSubPackageHiddenByLocalVariableInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test10", 
				new FileRange("Access/test10/Test.java", 13, 9, 13, 18),
				null);
	}

}
