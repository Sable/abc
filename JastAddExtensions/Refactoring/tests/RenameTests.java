package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(RenameVariableTests.class);
		suite.addTestSuite(RenameTypeTests.class);
		suite.addTestSuite(RenamePackageTests.class);
		suite.addTestSuite(RenameMethodTests.class);
		//$JUnit-END$
		return suite;
	}

}
