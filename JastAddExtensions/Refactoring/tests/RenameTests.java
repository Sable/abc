package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new RenameFieldTests("testRenameField"));
		suite.addTest(new RenameMethodTests("testRenameMethod"));
		suite.addTest(new RenameTypeTests("testRenameType"));
		return suite;
	}
}