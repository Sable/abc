package tests;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class PushDownMethodTests extends TestCase {
	public PushDownMethodTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		assertNotNull(out);
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPushDown();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPushDown();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
	}

    public void test1() {
    	testSucc(
    	    Program.fromClasses(
    	      "class A { void m() { } }",
    	      "class B extends A { }"),
    	    Program.fromClasses(
    	      "class A { }",
    	      "class B extends A { void m() { } }"));
    }

    public void test2() {
    	testSucc(
    	    Program.fromClasses(
    	      "class A { void m() { } }",
    	      "class B extends A { }",
    	      "class C { }"),
    	    Program.fromClasses(
    	      "class A { }",
    	      "class B extends A { void m() { } }",
    	      "class C { }"));
    }

    public void test3() {
    	// cannot push or A ceases to properly implement I
    	testFail(Program.fromClasses(
    		  "interface I { void m(); }",
    		  "class A implements I { public void m() { } }",
    		  "class B extends A { }"));
    }

    public void test4() {
    	// cannot push or A ceases to properly extend S 
    	testFail(Program.fromClasses(
    	           "abstract class S { abstract void m(); }",
    	           "class A extends S { void m() { } }",
    	           "class B extends A { }"));
    }

    public void test5() {
    	testFail(Program.fromClasses(
    	           "class A { void m() { } }",
    	           "class B extends A { }",
    	           "class C extends A { }"));
    }

    public void test6() {
    	testFail(Program.fromClasses("class A { void m() { } }"));
    }
    
    public void test7() {
    	testFail(Program.fromClasses(
    	           "class A { void m() { } }",
    	           "class B extends A { void m() { } }"));
    }
    
    public void test8() {
    	testFail(Program.fromClasses(
    	           "class A { void m() { } void f() { m(); } }",
    	           "class B extends A { }"));
    }
    
    public void test9() {
    	testFail(Program.fromClasses(
    	           "class A { void m() { } }",
    	           "class B extends A { }",
    	           "class C { { new A().m(); } }"));
    }
    
    public void test10() {
    	testSucc(Program.fromClasses(
    	           "class A { void m() { } }",
    	           "class B extends A { }",
    	           "class C { { new B().m(); } }"),
    	         Program.fromClasses(
    	    	   "class A { }",
    	    	   "class B extends A { void m() { } }",
    	    	   "class C { { new B().m(); } }"));
    }
    
    public void test11() {
    	testSucc(Program.fromClasses(
    			   "class A {" +
    			   "  int x = 23;" +
    			   "  void m() { System.out.println(x); }" +
    			   "}",
    			   "class B extends A { }"),
    			 Program.fromClasses(
    			   "class A {" +
    			   "  int x = 23;" +
    			   "}",
    			   "class B extends A {" +
    			   "  void m() { System.out.println(x); }" +
    			   "}"));
    }
    
    public void test12() {
    	testSucc(Program.fromClasses(
    			   "class A {" +
    			   "  int x = 23;" +
    			   "  void m() { System.out.println(x); }" +
    			   "}",
    			   "class B extends A { " +
    			   "  int x = 42;" +
    			   "}"),
    			 Program.fromClasses(
    			   "class A {" +
    			   "  int x = 23;" +
    			   "}",
    			   "class B extends A {" +
    			   "  int x = 42;" +
    			   "  void m() { System.out.println(super.x); }" +
    			   "}"));
    }
    
    public void test13() {
    	testFail(Program.fromClasses(
    			   "class S {" +
    			   "  void k() { }" +
    			   "}",
    			   "class A extends S {" +
    			   "  void m() { super.k(); }" +
    			   "  void k() { }" +
    			   "}",
    			   "class B extends A { }"));
    }
    
    public void test14() {
    	testSucc(Program.fromClasses(
    			   "class A {" +
    			   "  class B { }" +
    			   "  B m() { return new B(); }" +
    			   "}",
    			   "class C extends A {" +
    			   "  class B { }" +
    			   "}"),
    			 Program.fromClasses(
    			   "class A {" +
    			   "  class B { }" +
    			   "}",
    			   "class C extends A {" +
    			   "  class B { }" +
    			   "  A.B m() { return new A.B(); }" +
    			   "}"));
    }
    
    public void test15() {
    	testFail(Program.fromClasses(
    			   "class A {" +
    			   "  private void k() { }" +
    			   "  void m() { k(); }" +
    			   "}",
    			   "class B extends A { " +
    			   "  protected void k() { }" +
    			   "}"));
    }
    
    public void test16() {
    	testSucc(Program.fromClasses(
    			   "abstract class A {" +
    			   "  abstract void m();"+
    			   "}",
    			   "abstract class B extends A { }"),
    			 Program.fromClasses(
    			   "abstract class A { }",
    			   "abstract class B extends A { abstract void m(); }"));
    }
    
    public void test17() {
    	testSucc(Program.fromClasses(
    			   "interface A {" +
    			   "  void m();"+
    			   "}",
    			   "abstract class B implements A { }"),
    			 Program.fromClasses(
    			   "interface A { }",
    			   "abstract class B implements A { public abstract void m(); }"));
    }
    
    public void test18() {
    	testSucc(Program.fromClasses(
    			   "interface A {" +
    			   "  public void m();"+
    			   "}",
    			   "abstract class B implements A { }"),
    			 Program.fromClasses(
    			   "interface A { }",
    			   "abstract class B implements A { public abstract void m(); }"));
    }
    
    public void test19() {
    	testSucc(Program.fromClasses(
    			   "interface A {" +
    			   "  void m();"+
    			   "}",
    			   "interface B extends A { }"),
    			 Program.fromClasses(
    			   "interface A { }",
    			   "interface B extends A { void m(); }"));
    }

    public void test20() {
    	testSucc(
    	    Program.fromClasses(
    	      "class A { synchronized void m() { } }",
    	      "class B extends A { }"),
    	    Program.fromClasses(
    	      "class A { }",
    	      "class B extends A { synchronized void m() { } }"));
    }

    public void test22() {
    	testSucc(
    	    Program.fromClasses(
    	      "class A { static synchronized void m() { } }",
    	      "class B extends A { }"),
    	    Program.fromClasses(
    	      "class A { }",
    	      "class B extends A { static void m() { synchronized(A.class) { } } }"));
    }
    
    public void test23() {
    	testSucc(
    		Program.fromClasses(
    		  "class A { private int m() { return 23; }	}",
    		  "class Outer {" +
    		  "  int m () { return 42; }" +
    		  "  class B extends A {" +
    		  "    class Inner {" +
    		  "      int n() { return m(); }" +
    		  "    }" +
    		  "  }" +
    		  "}"),
   		    Program.fromClasses(
    	      "class A { }",
    	      "class Outer {" +
    	      "  int m () { return 42; }" +
    	      "  class B extends A {" +
    	      "    private int m() { return 23; }" +
    	      "    class Inner {" +
    	      "      int n() { return Outer.this.m(); }" +
    	      "    }" +
    	      "  }" +
   		      "}"));
    }

    public void test24() {
    	// see test3; OK in this case
    	testSucc(
    		Program.fromClasses(
    		  "interface I { void m(); }",
    		  "abstract class A implements I { public void m() { } }",
    		  "class B extends A { }"),
   		    Program.fromClasses(
    	      "interface I { void m(); }",
    	      "abstract class A implements I { }",
    	      "class B extends A { public void m() { } }"));
    }
    
    public void test25() {
    	testSucc(
    		Program.fromClasses(
    		  "class A { private int m(int i) { return i-19; } }",
    		  "class B extends A {" +
    		  "  long m(long i) { return i; }" +
    		  "  void m() { System.out.println(m(42)); }" +
    		  "}"),
    		Program.fromClasses(
    		  "class A { }",
    		  "class B extends A {" +
    		  "  long m(long i) { return i; }" +
    		  "  void m() { System.out.println(m((long)42)); }" +
    		  "  private int m(int i) { return i - 19; }" +
    		  "}"));
    }
    
    public void test26() {
    	testSucc(
    		Program.fromClasses(
    		  "class A { private int m(int i, int j) { return i-19; } }",
    		  "class B extends A {" +
    		  "  int m(int... i) { return i[0]; }" +
    		  "  void m() { System.out.println(m(42, 56)); }" +
    		  "}"),
    		Program.fromClasses(
    	      "class A { }",
    	      "class B extends A {" +
    	      "  int m(int... i) { return i[0]; }" +
    	      "  void m() { System.out.println(m(new int[] {42, 56})); }" +
    	      "  private int m(int i, int j) { return i-19; } " +
    		  "}"));
    }
    
    public void test27() {
    	testFail(
    		Program.fromClasses(
    		  "class Super { int m() { return 23; } }",
    		  "class A extends Super {" +
    		  "  int m() { return 42; }" +
    		  "  public static void main(String[] args) {" +
    		  "    Super s = new A();" +
    		  "    System.out.println(s.m());" +
    		  "  }" +
    		  "}",
    		  "class B extends A { }"));
    }
}
