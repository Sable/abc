package tests;

import AST.FileRange;

public class AccessSubSubPackageHiddenByParameterFromInsideMethod extends AccessPackage {

	public AccessSubSubPackageHiddenByParameterFromInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test9.pkg1", 
				new FileRange("Access/test9/Test.java", 12, 9, 12, 18),
				null);
	}

}
