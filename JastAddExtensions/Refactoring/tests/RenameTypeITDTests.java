package tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameTypeITDTests extends RenameType {
	
	public RenameTypeITDTests(String arg0) {
		super(arg0);
	}

	public static Test suite() {
		return new TestSuite(RenameTypeITDTests.class);
	}

	public String getTestBase() {
		return "RenameTypeITD";
	}

	public void test0() {
		runTypeRenameTest("test0");
	}
	public void test1() {
		runTypeRenameTest("test1");
	}
	public void test2() {
		runTypeRenameTest("test2");
	}
	public void test3() {
		runTypeRenameTest("test3");
	}
}