package test.cases;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenamingTests {
	
	public static Test suite() {
	    TestSuite suite= new TestSuite();
	    suite.addTest(new RenamingTest("test01"));
	    suite.addTest(new RenamingTest("test02"));
	    suite.addTest(new RenamingTest("test03"));
	    suite.addTest(new RenamingTest("test04"));
	    return suite;
	}

}
