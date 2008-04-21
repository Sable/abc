package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(AccessTests.suite());
		suite.addTest(EncapsulateFieldTests.suite());
		suite.addTest(ExtractMethodTests.suite());
		suite.addTest(RenameTests.suite());
		suite.addTest(RenameTestsITD.suite());
		suite.addTest(ExtractClassTests.suite());
		return suite;
	}
}
