package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(AccessTests.suite());
		suite.addTest(new EncapsulateFieldTests("testEncapsulateField"));
		suite.addTest(new ExtractMethodTests("testExtractMethod"));
		suite.addTest(RenameTests.suite());
		return suite;
	}
}
