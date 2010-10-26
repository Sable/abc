package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class PullUpMethodTests extends TestCase {
	public PullUpMethodTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPullUp();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPullUp();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

    public void test1() {
    	testSucc(
    	    Program.fromClasses(
    	      "class Super { }",
    	      "class A extends Super { void m() { } }"),
    	    Program.fromClasses(
    	      "class Super { void m() { } }",
    	      "class A extends Super { }"));
    }
    
    public void test2() {
    	testFail(
    		Program.fromClasses(
    		  "class Super { }",
    		  "class A extends Super { void m() { } }",
    		  "class B extends Super { int m() { return 23; } }"));
    }

    public void test3() {
    	testFail(
    		Program.fromClasses(
    		  "class Super { void m() { } }",
    		  "class A extends Super { void m() { } }"));
    }

    public void test4() {
    	testFail(
    		Program.fromClasses(
    		  "class SuperSuper { void m() { } }",
    		  "class Super extends SuperSuper { }",
    		  "class A extends Super { void m() { } }",
    		  "class B { { SuperSuper s = new Super(); s.m(); } }"));
    }
    
    public void test5() {
    	testSucc(
    		Program.fromClasses(
    		  "class Super { }",
    		  "class A extends Super { int m() { return 23; } }",
    		  "class B extends Super { int m() { return 42; } }"),
    		Program.fromClasses(
    	      "class Super { int m() { return 23; } }",
    	      "class A extends Super { }",
    	      "class B extends Super { int m() { return 42; } }"));
    }
    
    public void test6() {
    	testFail(
    		Program.fromClasses(
    		  "class SuperSuper { int m() { return 56; } }",
    		  "class Super extends SuperSuper { }",
    		  "class A extends Super { int m() { return 23; } }",
    		  "class B extends Super { int m() { return 42; } }",
    		  "class C { { SuperSuper s = new Super(); s.m(); } } "));
    }

    public void test7() {
    	testFail(
    		Program.fromClasses(
    		  "class SuperSuper { int m() { return 56; } }",
    		  "class Super extends SuperSuper { }",
    		  "class A extends Super { int m() { return 23; } }",
    		  "class B extends Super { int m() { return super.m(); } }"));
    }
    
    public void test8() {
    	testSucc(
    		Program.fromClasses(
    		  "class Super { }",
    		  "abstract class A extends Super { abstract void m(); }"),
    		Program.fromClasses(
    		  "abstract class Super { abstract void m(); }",
    		  "abstract class A extends Super { }"));
    }
    
    public void test9() {
    	testSucc(
    		Program.fromClasses(
    		  "class Super { int x; }",
    		  "class A extends Super {" +
    		  "  int x;" +
    		  "  int m() { return super.x; }" +
    		  "}"),
    		Program.fromClasses(
    		  "class Super {" +
    		  "  int x;" +
    		  "  int m() { return x; }" +
    		  "}",
    		  "class A extends Super { int x; }"));
    }
    
    public void test10() {
    	testSucc(
    		Program.fromCompilationUnits(
    		new RawCU("Super.java", "package p; public class Super { }"),
    		new RawCU("A.java", "package q; class A extends p.Super { int m() { return 23; } int x = m(); }")),
    		Program.fromCompilationUnits(
    		new RawCU("Super.java", "package p; public class Super { protected int m() { return 23; } }"),
    		new RawCU("A.java", "package q; class A extends p.Super { int x = m(); }")));
    }
    
    public void test11() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  A m() { return this; }" +
    			"}"));
    }
    
    public void test12() {
    	testSucc(
    		Program.fromClasses(
    			"class SuperSuper { }",
    			"class Super extends SuperSuper { }",
    			"class A extends Super {" +
    			"  private static void n(Super p) { }" +
    			"  public void m(SuperSuper q) {" +
    			"    n(this);" +
    			"  }" +
    			"}",
    			"class B extends Super {" +
    			"  private static void m(String r) { }" +
    			"  void f() { m(null); }" +
    			"}"),
    		Program.fromClasses(
       			"class SuperSuper { }",
       			"class Super extends SuperSuper {" +
       			"  public void m(SuperSuper q) {" +
       			"    A.n(this);" +
       			"  }" +
       			"}",
       			"class A extends Super {" +
       			"  static void n(Super p) { }" +
       			"}",
       			"class B extends Super {" +
       			"  private static void m(String r) { }" +
       			"  void f() { m((String)null); }" +
       			"}"));
    }
    
    /* rtt test 2010_10_19 11_39: org.w3c.jigadmin.RemoteResourceWrapper.getChildResource(java.lang.String) */
    public void test13() {
    	testFail(Program.fromClasses(
    			"class X { public X(A a){} } ",
				"class Super { }",
				"class A extends Super { void m(){new X(this);} }"));
    }
    
    public void test14() {
    	testFail(Program.fromClasses(
    			"class X { public void n(A a){} } ",
    			"class Super { }",
    			"class A extends Super { void m(){new X().n(this);} }"));
    }
    
    public void test15() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  private class B { }" +
    			"  void m() {" +
    			"    new Thread() {" +
    			"      B b;" +
    			"      public void run() { }" +
    			"    };" +
    			"  }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  void m() {" +
    			"    new Thread() {" +
    			"      A.B b;" +
    			"      public void run() { }" +
    			"    };" +
    			"  }" +
    			"}" +
    			"class A extends Super {" +
    			"  class B { }" +
    			"}"));
    }
    
    public void test16() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  private static void m() { }" +
    			"  { m(); }" +
    			"}",
    			"class B extends Super {" +
    			"  void m() { }" +
    			"}"));
    }
    
    public void test17() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  private static class Inner {" +
    			"    public Inner() { }" +
    			"  }" +
    			"  void m() {" +
    			"    new Inner();" +
    			"  }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  void m() {" +
    			"    new A.Inner();" +
    			"  }" +
    			"}" +
    			"class A extends Super {" +
    			"  static class Inner {" +
    			"    public Inner() { }" +
    			"  }" +
    			"}"));
    }
    
    public void test18() {
    	testSucc(Program.fromClasses(
    			"class SuperSuper {" +
    			"  int n() { return 23; }" +
    			"}",
    			"class Super extends SuperSuper {" +
    			"}",
    			"class A extends Super {" +
    			"  int n() { return 42; }" +
    			"  int m() { return super.n(); }" +
    			"}",
    			"class Main {" +
    			"  public static void main(String[] args) {" +
    			"    System.out.println(new A().m());" +
    			"  }" +
    			"}"),
    			Program.fromClasses(
    			"class SuperSuper {" +
    			"  int n() { return 23; }" +
    			"}",
    			"class Super extends SuperSuper {" +
    			"  int m() { return super.n(); }" +
    			"}",
    			"class A extends Super {" +
    			"  int n() { return 42; }" +
    			"}",
    			"class Main {" +
    			"  public static void main(String[] args) {" +
    			"    System.out.println(new A().m());" +
    			"  }" +
    			"}"));
    }
    
    public void test19() {
    	testSucc(Program.fromClasses(
    			"class Super {" +
    			"  int x;" +
    			"}",
    			"class A extends Super {" +
    			"  int x;" +
    			"  int m() { return super.x; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  int x;" +
    			"  int m() { return x; }" +
    			"}",
    			"class A extends Super {" +
    			"  int x;" +
    			"}"));
    }
    
    public void test20() {
    	testSucc(Program.fromClasses(
    			"class Super {" +
    			"  int x;" +
    			"}",
    			"class A extends Super {" +
    			"  int x;" +
    			"  int m() { return A.super.x; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  int x;" +
    			"  int m() { return x; }" +
    			"}",
    			"class A extends Super {" +
    			"  int x;" +
    			"}"));
    }
}
