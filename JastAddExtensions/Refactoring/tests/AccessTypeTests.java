package tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import AST.Dot;
import AST.FileRange;
import AST.TypeAccess;

public class AccessTypeTests extends AccessType {

	public AccessTypeTests(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(AccessTypeTests.class);
	}

	public void test0() {
		runTypeAccessTest(new FileRange("AccessType/test16/Test.java", 18, 5, 18, 19), new FileRange("AccessType/test16/Test.java", 12, 19, 12, 19), new TypeAccess("B"));
	}
	public void test1() {
		runTypeAccessTest(new FileRange("AccessType/test17/Test.java", 14, 5, 16, 5), new FileRange("AccessType/test17/Test.java", 12, 16, 12, 20), new TypeAccess("A"));
	}		
	public void test2() {
		runTypeAccessTest(new FileRange("AccessType/test17/Test.java", 15, 9, 15, 19), new FileRange("AccessType/test17/Test.java", 12, 19, 12, 19), new TypeAccess("B"));
	}	
	public void test3() {
		runTypeAccessTest(new FileRange("AccessType/test19/Test.java", 11, 1, 16, 1), new FileRange("AccessType/test19/Test.java", 14, 17, 14, 19), new TypeAccess("Access.test19", "Test"));
	}	
	public void test4() {
		runTypeAccessTest(new FileRange("AccessType/test20/Test.java", 12, 6, 12, 40), new FileRange("AccessType/test20/Test.java", 15, 16, 15, 16), new Dot(new TypeAccess("Test"), new TypeAccess("Foo")));
	}	
	public void test5() {
		runTypeAccessTest(new FileRange("AccessType/test21/Test.java", 10, 1, 15, 1), new FileRange("AccessType/test21/Test.java", 13, 16, 13, 16), new TypeAccess("Test"));
	}	
	public void test6() {
		runTypeAccessTest(new FileRange("AccessType/test21/pkg1/Test.java", 3, 1, 5, 1), new FileRange("AccessType/test21/Test.java", 13, 16, 13, 16), new TypeAccess("Access.test21.pkg1", "Test"));
	}	
	public void test7() {
		runTypeAccessTest(new FileRange("AccessType/test23/Test.java", 10, 1, 11, 1), new FileRange("AccessType/test23/Test.java", 14, 5, 14, 7), new TypeAccess("Test"));
	}	
	public void test8() {
		runTypeAccessTest(new FileRange("AccessType/test39/A.java", 5, 5, 7, 5), new FileRange("AccessType/test39/A.java", 16, 7, 16, 9), new TypeAccess("XYZ"));
	}	
	public void test9() {
		runTypeAccessTest(new FileRange("AccessType/test40/Test.java", 12, 5, 17, 5), new FileRange("AccessType/test40/Test.java", 15, 27, 15, 39), new TypeAccess("Inner1"));
	}	
	public void test10() {
		runTypeAccessTest(new FileRange("AccessType/test41/Test.java", 4, 5, 10, 5), new FileRange("AccessType/test41/Test.java", 8, 30, 8, 32), new Dot(new TypeAccess("A"), new TypeAccess("B")));
	}	
	public void test11() {
		runTypeAccessTest("java.lang", "String", new FileRange("AccessType/test42/Test.java", 5, 17, 5, 20), new TypeAccess("String"));
	}	
	public void test12() {
		runTypeAccessTest("java.lang", "String", new FileRange("AccessType/test43/Test.java", 6, 17, 6, 28), new TypeAccess("java.lang", "String"));
	}	
	public void test13() {
		runTypeAccessTest("java.lang", "String", new FileRange("AccessType/test44/Test.java", 7, 17, 7, 20), null);
	}	
	public void test14() {
		runTypeAccessTest("java.lang", "String", new FileRange("AccessType/test45/Test.java", 7, 17, 7, 20), null);
	}
	public void test15() {
		runTypeAccessTest(new FileRange("AccessType/test15/Test.java", 17, 1, 19, 1), new FileRange("AccessType/test15/Test.java", 13, 16, 13, 16), new TypeAccess("A"));
	}
	public void test16() {
		runTypeAccessTest(new FileRange("AccessType/test46/Test.java", 5, 5, 11, 5), new FileRange("AccessType/test46/Test.java", 9, 30, 9, 32), null);
	}
	public void test17() {
		runTypeAccessTest("java.lang", "String", new FileRange("AccessType/test47/Test.java", 3, 2, 3, 20), new TypeAccess("java.lang", "String"));
	}
	public void test18() {
		runTypeAccessTest(new FileRange("AccessType/test39/A.java", 5, 5, 7, 5), new FileRange("AccessType/test39/A.java", 16, 28, 16, 28), new TypeAccess("XYZ"));
	}	

}
