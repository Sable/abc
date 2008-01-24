package tests;

import AST.FileRange;

public class AccessSubPackageFromTopLevel extends AccessPackage {

	public AccessSubPackageFromTopLevel(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test1", 
				new FileRange("Access/test1/Test.java", 11, 1, 13, 1),
				"Access.test1");
	}

}
