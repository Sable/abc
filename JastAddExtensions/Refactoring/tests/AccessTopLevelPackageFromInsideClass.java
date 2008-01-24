package tests;

import AST.FileRange;

public class AccessTopLevelPackageFromInsideClass extends AccessPackage {

	public AccessTopLevelPackageFromInsideClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test1/Test.java", 12, 5, 12, 20),
				"Access");
	}

}
