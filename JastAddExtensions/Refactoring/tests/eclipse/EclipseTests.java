package tests.eclipse;

import junit.framework.Test;
import junit.framework.TestSuite;

public class EclipseTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for tests.eclipse");
		//$JUnit-BEGIN$
		suite.addTestSuite(tests.eclipse.ExtractTemp.ExtractTempTests.class);
		suite.addTestSuite(tests.eclipse.MoveInstanceMethod.MoveInstanceMethodTests.class);
		suite.addTestSuite(tests.eclipse.PushDown.PushDownTests.class);
		suite.addTestSuite(tests.eclipse.PullUp.PullUpTests.class);
		suite.addTestSuite(tests.eclipse.RenamePackage.RenamePackageTests.class);
		suite.addTestSuite(tests.eclipse.MoveInnerToTopLevel.MoveInnerToTopLevelTests.class);
		suite.addTestSuite(tests.eclipse.ConvertAnonymousToNested.ConvertAnonymousToNestedTests.class);
		suite.addTestSuite(tests.eclipse.SelfEncapsulateField.SelfEncapsulateFieldTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceParameterObject.IntroduceParameterObjectTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceIndirection.IntroduceIndirectionTests.class);
		suite.addTestSuite(tests.eclipse.ExtractClass.ExtractClassTests.class);
		suite.addTestSuite(tests.eclipse.MoveMembers.MoveMembersTests.class);
		suite.addTestSuite(tests.eclipse.ExtractConstant.ExtractConstantTests.class);
		suite.addTestSuite(tests.eclipse.InlineConstant.InlineConstantTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceParameter.IntroduceParameterTests.class);
		suite.addTestSuite(tests.eclipse.PromoteTempToField.PromoteTempToFieldTests.class);
		suite.addTestSuite(tests.eclipse.InlineTemp.InlineTempTests.class);
		suite.addTestSuite(tests.eclipse.IntroduceFactory.IntroduceFactoryTests.class);
		//$JUnit-END$
		return suite;
	}

}
