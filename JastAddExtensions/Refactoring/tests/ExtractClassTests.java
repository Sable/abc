package tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class ExtractClassTests extends ExtractClass {

	public ExtractClassTests(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(ExtractClassTests.class);
	}

	public void test1() {
		runExtractClassTest("test1");
	}
	
	public void test2() {
		runExtractClassTest("test2");
	}
	
	public void test3() {
		runExtractClassTest("test3");
	}

	public void test4() {
		runExtractClassTest("test4");
	}

}
