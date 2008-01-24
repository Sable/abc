package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AccessPackageTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new AccessPackageHiddenByClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageFromInsideClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageFromInsideMethod("testPackageAccess"));
		suite.addTest(new AccessSubPackageFromTopLevel("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByClassFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByFieldFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByField("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByLocalClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByLocalVariableFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByLocalVariableInsideMethod("testPackageAccess"));
		suite.addTest(new AccessSubPackageHiddenByParameterFromInsideMethod("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageFromInsideClass("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageFromInsideMethod("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageFromTopLevel("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageHiddenByClassFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageHiddenByFieldFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageHiddenByLocalVariableFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageHiddenByLocalVariableInsideMethod("testPackageAccess"));
		suite.addTest(new AccessSubSubPackageHiddenByParameterFromInsideMethod("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageFromInsideClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageFromInsideInnerClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageFromInsideMethod("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageFromTopLevel("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByClassFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByFieldFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByField("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByLocalClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByLocalVariableFromInsideAnonymousClass("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByLocalVariableInsideMethod("testPackageAccess"));
		suite.addTest(new AccessTopLevelPackageHiddenByParameterFromInsideMethod("testPackageAccess"));
		return suite;
	}
}