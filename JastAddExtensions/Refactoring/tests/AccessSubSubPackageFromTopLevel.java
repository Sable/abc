package tests;

import AST.FileRange;

public class AccessSubSubPackageFromTopLevel extends AccessPackage {

	public AccessSubSubPackageFromTopLevel(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test1.pkg1", 
				new FileRange("Access/test1/Test.java", 11, 1, 13, 1),
				"Access.test1.pkg1");
	}

}
