package tests;

import java.io.File;

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
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test24"+File.separator+"Test.java", 12, 5, 12, 11), new FileRange("AccessField"+File.separator+"test24"+File.separator+"Test.java", 13, 14, 13, 14), new VarAccess("a"));
	}
	
	public void test1() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test25"+File.separator+"Test.java", 12, 5, 12, 11), new FileRange("AccessField"+File.separator+"test25"+File.separator+"Test.java", 13, 7, 13, 7), new VarAccess("a"));
	}
	
	public void test2() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test26"+File.separator+"Test.java", 11, 5, 11, 14), new FileRange("AccessField"+File.separator+"test26"+File.separator+"Test.java", 16, 7, 16, 15), new Dot(new SuperAccess("super"), new VarAccess("foo")));
	}
	
	public void test3() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test27"+File.separator+"Test.java", 12, 5, 12, 14), new FileRange("AccessField"+File.separator+"test27"+File.separator+"Test.java", 19, 24, 19, 32), new Dot(new SuperAccess("super"), new VarAccess("foo")));
	}
	
	public void test4() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test28"+File.separator+"Test.java", 12, 5, 12, 14), new FileRange("AccessField"+File.separator+"test28"+File.separator+"Test.java", 20, 24, 20, 36), new Dot(new ParExpr(new CastExpr(new TypeAccess("A"), new ThisAccess("this"))), new VarAccess("foo")));
	}
	
	public void test5() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test29"+File.separator+"Test.java", 11, 6, 11, 13), new FileRange("AccessField"+File.separator+"test29"+File.separator+"Test.java", 16, 14, 16, 25), new Dot(new TypeAccess("A"), new VarAccess("foo")));
	}
	
	public void test6() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test30"+File.separator+"Test.java", 12, 6, 12, 13), new FileRange("AccessField"+File.separator+"test30"+File.separator+"Test.java", 19, 25, 19, 36), new Dot(new TypeAccess("B"), new VarAccess("foo")));
	}

	public void test7() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test31"+File.separator+"Test.java", 12, 5, 12, 14), new FileRange("AccessField"+File.separator+"test31"+File.separator+"Test.java", 20, 24, 20, 36), new Dot(new TypeAccess("A"), new VarAccess("foo")));
	}

	public void test8() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test32"+File.separator+"Test.java", 11, 5, 11, 19), new FileRange("AccessField"+File.separator+"test32"+File.separator+"Test.java", 13, 16, 13, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
	}
	
	public void test9() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test33"+File.separator+"Test.java", 11, 5, 11, 19), new FileRange("AccessField"+File.separator+"test33"+File.separator+"Test.java", 14, 16, 14, 23), new Dot(new ThisAccess("this"), new VarAccess("bar")));
	}
	
	public void test10() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test34"+File.separator+"Test.java", 11, 5, 11, 19), new FileRange("AccessField"+File.separator+"test34"+File.separator+"Test.java", 18, 16, 18, 23), new Dot(new SuperAccess("super"), new VarAccess("bar")));
	}
	
	public void test11() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test35"+File.separator+"Test.java", 11, 5, 11, 19), new FileRange("AccessField"+File.separator+"test35"+File.separator+"Test.java", 14, 24, 14, 26), new VarAccess("bar"));
	}
	
	public void test12() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test36"+File.separator+"Test.java", 11, 5, 11, 19), new FileRange("AccessField"+File.separator+"test36"+File.separator+"Test.java", 15, 27, 15, 39), new Dot(new TypeAccess("Test"), new Dot(new ThisAccess("this"), new VarAccess("bar"))));
	}
	
	public void test13() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test37"+File.separator+"Test.java", 11, 5, 11, 12), new FileRange("AccessField"+File.separator+"test37"+File.separator+"Test.java", 17, 24, 17, 26), new VarAccess("bar"));
	}
	
	public void test14() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test38"+File.separator+"Test.java", 12, 5, 12, 12), new FileRange("AccessField"+File.separator+"test38"+File.separator+"Test.java", 19, 24, 19, 37), new Dot(new TypeAccess("Test"), new Dot(new SuperAccess("super"), new VarAccess("bar"))));
	}

	public void test15() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test26"+File.separator+"Test.java", 11, 5, 11, 14), new FileRange("AccessField"+File.separator+"test26"+File.separator+"Test.java", 16, 13, 16, 15), 
						   new SuperAccess("super").qualifiesAccess(new VarAccess("foo")));
	}

	public void test16() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test39"+File.separator+"Test.java", 12, 5, 12, 20), new FileRange("AccessField"+File.separator+"test39"+File.separator+"Test.java", 17, 14, 17, 16), new Dot(new TypeAccess("A"), new VarAccess("foo")));
	}

	public void test17() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test40"+File.separator+"Test.java", 16, 5, 16, 14), new FileRange("AccessField"+File.separator+"test40"+File.separator+"Test.java", 19, 21, 19, 30), 
					new TypeAccess("Test").qualifiesAccess(new ThisAccess("this").qualifiesAccess(new VarAccess("foo"))));
	}

	public void test18() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 6, 9, 6, 13), new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 14, 18, 14, 18), 
					new VarAccess("x").qualifiesAccess(new VarAccess("i")));
	}

	public void test19() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 4, 5, 4, 9), new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 14, 18, 14, 18), 
						   null);
	}

	public void test20() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 6, 9, 6, 13), new FileRange("AccessField"+File.separator+"test41"+File.separator+"Test.java", 14, 16, 14, 18), 
						   null);
	}

	public void test21() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test42"+File.separator+"Test.java", 4, 5, 4, 9), new FileRange("AccessField"+File.separator+"test42"+File.separator+"Test.java", 14, 23, 14, 23), 
						   new ParExpr(new CastExpr(new TypeAccess("A"), new VarAccess("b"))).qualifiesAccess(new VarAccess("i")));
	}

	public void test22() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test43"+File.separator+"Test.java", 4, 5, 4, 9), new FileRange("AccessField"+File.separator+"test43"+File.separator+"Test.java", 14, 18, 14, 18), 
						   new ParExpr(new CastExpr(new TypeAccess("A"), new VarAccess("b"))).qualifiesAccess(new VarAccess("i")));
	}

	public void test23() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test44"+File.separator+"Test.java", 4, 5, 4, 9), new FileRange("AccessField"+File.separator+"test44"+File.separator+"Test.java", 15, 20, 15, 20), 
						   new ParExpr(new CastExpr(new TypeAccess("A"), 
								   				    new VarAccess("b").qualifiesAccess(new VarAccess("b")))).qualifiesAccess(new VarAccess("i")));
	}

	public void test24() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test45"+File.separator+"Test.java", 4, 5, 4, 16), new FileRange("AccessField"+File.separator+"test45"+File.separator+"Test.java", 13, 18, 13, 18),
						   null);
	}

	public void test25() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test46"+File.separator+"Test.java", 8, 5, 8, 12), new FileRange("AccessField"+File.separator+"test46"+File.separator+"Test.java", 10, 20, 10, 30),
						   new TypeAccess("Test").qualifiesAccess(new ThisAccess("this").qualifiesAccess(new VarAccess("x"))));
	}

	public void test26() {
		runFieldAccessTest(new FileRange("AccessField"+File.separator+"test47"+File.separator+"Test.java", 4, 5, 4, 13), new FileRange("AccessField"+File.separator+"test47"+File.separator+"Test.java", 8, 16, 8, 16),
						   new VarAccess("x"));
	}

}