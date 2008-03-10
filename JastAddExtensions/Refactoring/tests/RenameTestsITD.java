package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTestsITD {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(RenameVariableITDTests.suite());
		suite.addTest(RenameMethodITDTests.suite());
		suite.addTest(RenameTypeITDTests.suite());
		return suite;
	}
}