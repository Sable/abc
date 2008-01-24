package tests;

import AST.FileRange;

public class AccessSubSubPackageHiddenByLocalVariableFromInsideAnonymousClass extends AccessPackage {

	public AccessSubSubPackageHiddenByLocalVariableFromInsideAnonymousClass(String arg0) {
		super(arg0);
	}
	
	public void testPackageAccess() {
		runPackageAccessTest("Access.test12.pkg1", 
				new FileRange("Access/test12/Test.java", 15, 26, 15, 36),
				null);
	}

}
