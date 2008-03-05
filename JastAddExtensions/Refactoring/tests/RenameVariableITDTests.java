package tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameVariableITDTests extends RenameVariable {

	protected String getTestBase() {
		return "RenameVariableITD";
	}

	public RenameVariableITDTests(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(RenameVariableITDTests.class);
	}

	public void test1() {
		runFieldRenameTest("test1");
	}
	public void test2() {
		runFieldRenameTest("test2");
	}
	public void test3() {
		runFieldRenameTest("test3");
	}
}
