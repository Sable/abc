package tests;

import AST.FileRange;

public class AccessSubPackageFromInsideClass extends AccessPackage {

	public AccessSubPackageFromInsideClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test1", 
				new FileRange("Access/test1/Test.java", 12, 5, 12, 20),
				"Access.test1");
	}

}
