package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WrapBlockInClosureTests extends WrapBlockInClosure {

	public WrapBlockInClosureTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(WrapBlockInClosureTests.class);
	}

	public void test1() {
		runWrappingTest("test1");
	}
	public void test2() {
		runWrappingTest("test2");
	}
	public void test3() {
		runWrappingTest("test3");
	}
	public void test4() {
		runWrappingTest("test4");
	}
	public void test5() {
		runWrappingTest("test5");
	}
	public void test6() {
		runWrappingTest("test6");
	}
	public void test7() {
		runWrappingTest("test7");
	}
	public void test8() {
		runWrappingTest("test8");
	}
}
