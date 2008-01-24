package tests;

import AST.FileRange;

public class AccessTopLevelPackageHiddenByLocalVariableFromInsideAnonymousClass extends AccessPackage {

	public AccessTopLevelPackageHiddenByLocalVariableFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access", 
				new FileRange("Access/test12/Test.java", 15, 26, 15, 36),
				null);
	}

}
