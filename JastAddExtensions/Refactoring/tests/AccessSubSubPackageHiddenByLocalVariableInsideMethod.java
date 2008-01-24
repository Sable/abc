package tests;

import AST.FileRange;

public class AccessSubSubPackageHiddenByLocalVariableInsideMethod extends AccessPackage {

	public AccessSubSubPackageHiddenByLocalVariableInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test10.pkg1", 
				new FileRange("Access/test10/Test.java", 13, 9, 13, 18),
				null);
	}

}
