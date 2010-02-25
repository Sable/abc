package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Refactoring tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(RenameMethodTests.class);
		suite.addTestSuite(ExtractTempTests.class);
		suite.addTestSuite(RenameVariableTests.class);
		suite.addTestSuite(PushDownMethodTests.class);
		suite.addTestSuite(InlineMethodTests.class);
		suite.addTestSuite(RenameTypeTests.class);
		suite.addTestSuite(RenamePackageTests.class);
		suite.addTestSuite(MoveMethodTests.class);
		suite.addTestSuite(MakeMethodStaticTests.class);
		suite.addTestSuite(InlineTempTests.class);
		suite.addTestSuite(RemoveUnusedMethodTests.class);
		suite.addTestSuite(PullUpMethodTests.class);
		suite.addTestSuite(MoveMemberTypeToToplevelTests.class);
		suite.addTestSuite(tests.eclipse.ExtractTemp.ExtractTempTests.class);
		suite.addTestSuite(tests.eclipse.MoveInstanceMethod.MoveInstanceMethodTests.class);
		suite.addTestSuite(tests.eclipse.PushDown.PushDownTests.class);
		suite.addTestSuite(tests.eclipse.PullUp.PullUpTests.class);
		suite.addTestSuite(tests.eclipse.RenamePackage.RenamePackageTests.class);
		suite.addTestSuite(tests.eclipse.MoveInnerToTopLevel.MoveInnerToTopLevelTests.class);
		suite.addTestSuite(tests.eclipse.ConvertAnonymousToNested.ConvertAnonymousToNestedTests.class);
		suite.addTestSuite(tests.eclipse.SelfEncapsulateField.SelfEncapsulateFieldTests.class);
		//$JUnit-END$
		return suite;
	}

}
