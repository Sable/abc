package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Refactoring tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(RenameMethodTests.class);
		suite.addTestSuite(ExtractTempTests.class);
		suite.addTestSuite(RenameVariableTests.class);
		suite.addTestSuite(PushDownMethodTests.class);
		suite.addTestSuite(InlineMethodTests.class);
		suite.addTestSuite(RenameTypeTests.class);
		suite.addTestSuite(MoveMethodTests.class);
		suite.addTestSuite(MakeMethodStaticTests.class);
		suite.addTestSuite(InlineTempTests.class);
		//$JUnit-END$
		return suite;
	}

}
