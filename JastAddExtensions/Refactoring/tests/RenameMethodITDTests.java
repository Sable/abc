package tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameMethodITDTests extends RenameMethod {

	public RenameMethodITDTests(String arg0) {
		super(arg0);
	}

	public static Test suite() {
		return new TestSuite(RenameMethodITDTests.class);
	}
	
	protected String getTestBase() {
		return "RenameMethodITD";
	}

	public void test1() {
		runMethodRenameTest("test1");
	}
	public void test2() {
		runMethodRenameTest("test2");
	}
	public void test3() {
		runMethodRenameTest("test3");
	}
	public void test4() {
		runMethodRenameTest("test4");
	}
	public void test5() {
		runMethodRenameTest("test5");
	}
	public void test6() {
		runMethodRenameTest("test6");
	}
}