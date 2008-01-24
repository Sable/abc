package tests;

import AST.FileRange;

public class AccessTopLevelPackageFromInsideInnerClass extends AccessPackage {

	public AccessTopLevelPackageFromInsideInnerClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test5/Test.java", 13, 9, 13, 30),
				"Access");
	}

}
