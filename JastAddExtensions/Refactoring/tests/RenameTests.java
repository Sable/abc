package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(RenameVariableTests.suite());
		suite.addTest(RenameMethodTests.suite());
		suite.addTest(RenameTypeTests.suite());
		return suite;
	}
}