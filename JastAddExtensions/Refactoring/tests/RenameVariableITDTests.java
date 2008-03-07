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
	public void test4() {
		runFieldRenameTest("test4");
	}
	public void test5() {
		runFieldRenameTest("test5");
	}
	public void test6() {
		runFieldRenameTest("test6");
	}
	public void test7() {
		runFieldRenameTest("test7");
	}
	public void test8() {
		runFieldRenameTest("test8");
	}
	public void test9() {
		runFieldRenameTest("test9");
	}
	public void test10() {
		runFieldRenameTest("test10");
	}
}
