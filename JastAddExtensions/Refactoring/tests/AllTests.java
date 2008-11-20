package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(AccessTests.suite());
		suite.addTest(EncapsulateFieldTests.suite());
		suite.addTest(PushStatementIntoBlockTests.suite());
		suite.addTest(ExtractBlockTests.suite());
		suite.addTest(WrapBlockInClosureTests.suite());
		suite.addTest(ClosureConversionTests.suite());
		suite.addTest(ExtractMethodTests.suite());
		suite.addTest(RenameTests.suite());
		suite.addTest(RenameTestsITD.suite());
		suite.addTest(ExtractClassTests.suite());
		suite.addTest(InlineLocalVariableTests.suite());
		suite.addTest(PushDownMethodTests.suite());
		suite.addTest(InlineMethodTests.suite());
		suite.addTest(ExtractLocalVariableTests.suite());
		return suite;
	}
}
