package tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ClosureConversionTests extends ClosureConversion {

	public ClosureConversionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ClosureConversionTests.class);
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
/*	public void test5() {
		runTest("test5");
	}*/
}
