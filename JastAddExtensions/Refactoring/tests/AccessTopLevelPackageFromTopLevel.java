package tests;

import AST.FileRange;

public class AccessTopLevelPackageFromTopLevel extends AccessPackage {

	public AccessTopLevelPackageFromTopLevel(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test1/Test.java", 11, 1, 13, 1),
				"Access");
	}

}
