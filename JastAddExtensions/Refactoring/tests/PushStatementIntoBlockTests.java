package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PushStatementIntoBlockTests extends PushStatementIntoBlock {

	public PushStatementIntoBlockTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(PushStatementIntoBlockTests.class);
	}

	public void test1() {
		runTest("test1");
	}
	public void test2() {
		runTest("test2");
	}
	public void test3() {
		runTest("test3");
	}
	public void test4() {
		runTest("test4");
	}
	public void test5() {
		runTest("test5");
	}
	public void test6() {
		runTest("test6");
	}
	public void test7() {
		runTest("test7");
	}
	public void test8() {
		runTest("test8");
	}
}
