package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllExtractMethodTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(PushStatementIntoBlockTests.suite());
		suite.addTest(ExtractBlockTests.suite());
		suite.addTest(WrapBlockInClosureTests.suite());
		suite.addTest(ClosureConversionTests.suite());
		suite.addTest(ExtractMethodTests.suite());
		return suite;
	}
}
