package tests;

import AST.FileRange;

public class AccessSubPackageFromInsideMethod extends AccessPackage {

	public AccessSubPackageFromInsideMethod(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test8", 
				new FileRange("Access/test8/Test.java", 13, 9, 13, 14),
				"Access.test8");
	}

}
