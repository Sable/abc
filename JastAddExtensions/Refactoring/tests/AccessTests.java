package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AccessTests {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(AccessPackageTests.suite());
		suite.addTest(new AccessTypeTests("testTypeAccess"));
		suite.addTest(new AccessFieldTests("testFieldAccess"));
		return suite;
	}
}