package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Rename tests");
		suite.addTest(RenameVariableTests.suite());
		suite.addTest(RenameTypeTests.suite());
		suite.addTest(RenamePackageTests.suite());
		suite.addTest(RenameMethodTests.suite());
		return suite;
	}

}
