package tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import AST.CastExpr;
import AST.Dot;
import AST.FileRange;
import AST.ParExpr;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.VarAccess;

public class AccessFieldTests extends AccessField {

	public AccessFieldTests(String arg0) {
		super(arg0);
	}

	public static Test suite() {
		return new TestSuite(AccessFieldTests.class);
	}
	public void test0() {
		runFieldAccessTest(new FileRange("AccessField/test24/Test.java", 12, 5, 12, 11), new FileRange("AccessField/test24/Test.java", 13, 14, 13, 14), new VarAccess("a"));
	}
	
	public void test1() {
		runFieldAccessTest(new FileRange("AccessField/test25/Test.java", 12, 5, 12, 11), new FileRange("AccessField/test25/Test.java", 13, 7, 13, 7), new VarAccess("a"));
	}
	
	public void test2() {
		runFieldAccessTest(new FileRange("AccessField/test26/Test.java", 11, 5, 11, 14), new FileRange("AccessField/test26/Test.java", 16, 7, 16, 15), new Dot(new SuperAccess("super"), new VarAccess("foo")));
	}
	
	public void test3() {
		runFieldAccessTest(new FileRange("AccessField/test27/Test.java", 12, 5, 12, 14), new FileRange("AccessField/test27/Test.java", 19, 24, 19, 32), new Dot(new SuperAccess("super"), new VarAccess("foo")));
	}
	
	public void test4() {
		runFieldAccessTest(new FileRange("AccessField/test28/Test.java", 12, 5, 12, 14), new FileRange("AccessField/test28/Test.java", 20, 24, 20, 36), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
	}
	
	public void test5() {
		runFieldAccessTest(new FileRange("AccessField/test29/Test.java", 11, 6, 11, 13), new FileRange("AccessField/test29/Test.java", 16, 14, 16, 25), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
	}
	
	public void test6() {
		runFieldAccessTest(new FileRange("AccessField/test30/Test.java", 12, 6, 12, 13), new FileRange("AccessField/test30/Test.java", 19, 25, 19, 36), new Dot(new ParExpr(new CastExpr(new TypeAccess("B"), new ThisAccess("this"))), new VarAccess("foo")));
	}

	public void test7() {
		runFieldAccessTest(new FileRange("AccessField/test31/Test.java", 12, 5, 12, 14), new FileRange("AccessField/test31/Test.java", 20, 24, 20, 36), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
	}

	public void test8() {
		runFieldAccessTest(new FileRange("AccessField/test32/Test.java", 11, 5, 11, 19), new FileRange("AccessField/test32/Test.java", 13, 16, 13, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
	}
	
	public void test9() {
		runFieldAccessTest(new FileRange("AccessField/test33/Test.java", 11, 5, 11, 19), new FileRange("AccessField/test33/Test.java", 14, 16, 14, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
	}
	
	public void test10() {
		runFieldAccessTest(new FileRange("AccessField/test34/Test.java", 11, 5, 11, 19), new FileRange("AccessField/test34/Test.java", 18, 16, 18, 23), new Dot(new SuperAccess("super"), new VarAccess("bar")));
	}
	
	public void test11() {
		runFieldAccessTest(new FileRange("AccessField/test35/Test.java", 11, 5, 11, 19), new FileRange("AccessField/test35/Test.java", 14, 24, 14, 26), new VarAccess("bar"));
	}
	
	public void test12() {
		runFieldAccessTest(new FileRange("AccessField/test36/Test.java", 11, 5, 11, 19), new FileRange("AccessField/test36/Test.java", 15, 27, 15, 39), new Dot(new TypeAccess("Test"), new Dot(new ThisAccess("this"), new VarAccess("bar"))));
	}
	
	public void test13() {
		runFieldAccessTest(new FileRange("AccessField/test37/Test.java", 11, 5, 11, 12), new FileRange("AccessField/test37/Test.java", 17, 24, 17, 26), new VarAccess("bar"));
	}
	
	public void test14() {
		runFieldAccessTest(new FileRange("AccessField/test38/Test.java", 12, 5, 12, 12), new FileRange("AccessField/test38/Test.java", 19, 24, 19, 37), new Dot(new TypeAccess("Test"), new Dot(new SuperAccess("super"), new VarAccess("bar"))));
	}

}