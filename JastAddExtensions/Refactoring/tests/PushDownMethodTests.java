package tests;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
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
			md.doPushDown(false);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.getMessage());
		}
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPushDown(false);
			assertEquals("<failure>", in.toString());
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
    		  "class B extends A { { new A(); } }"));
    }

    public void test4() {
    	// cannot push or A ceases to properly extend S 
    	testFail(Program.fromClasses(
    	           "abstract class S { abstract void m(); }",
    	           "class A extends S { void m() { } }",
    	           "class B extends A { { new A(); } }"));
    }

    public void test5() {
    	testSucc(
    		Program.fromClasses(
    		  "class A { void m() { } }",
    		  "class B extends A { }",
    		  "class C extends A { }"),
    		Program.fromClasses(
    		  "class A { }",
    		  "class B extends A { void m() { } }",
    		  "class C extends A { void m() { } }"));
    }

    public void test6() {
    	testSucc(Program.fromClasses("class A { void m() { } }"),
    			 Program.fromClasses("class A { }"));
    }
    
    public void test7() {
    	testSucc(Program.fromClasses(
    	           "class A { void m() { } }",
    	           "class B extends A { void m() { } }"),
    	         Program.fromClasses(
    	    	   "class A { }",
    	           "class B extends A { void m() { } }"));
    }
    
    public void test8() {
    	testSucc(Program.fromClasses(
    	           "class A { void m() { } void f() { m(); } }",
    	           "class B extends A { }"),
    	         Program.fromClasses(
    	    	   "abstract class A { abstract void m(); void f() { m(); } }",
    	    	   "class B extends A { void m() { } }"));
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
    	      "    class Inner {" +
    	      "      int n() { return Outer.this.m(); }" +
    	      "    }" +
    	      "    private int m() { return 23; }" +
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
    
    public void test28() {
    	testSucc(
    		Program.fromClasses(
    		  "interface I { void m(); }",
    		  "class A implements I { public void m() { } }",
    		  "class B extends A { }"),
    		Program.fromClasses(
    		  "interface I { void m(); }",
    		  "abstract class A implements I { }",
    		  "class B extends A { public void m() { } }"));
    }
    
    public void test29() {
    	testSucc(
    		Program.fromClasses(
    		  "interface I { void m(); }",
    		  "class A implements I { " +
    		  "  int i; " +
    		  "  A(int i) { this.i = i; } " +
    		  "  public void m() { }" +
    		  "}",
    		  "class B extends A {" +
    		  "  B() { super(23); }" +
    		  "}"),
    		Program.fromClasses(
    	      "interface I { void m(); }",
    	      "abstract class A implements I { " +
    	      "  int i; " +
    	   	  "  A(int i) { this.i = i; } " +
    	   	  "}",
    	   	  "class B extends A {" +
    	   	  "  B() { super(23); }" +
    	   	  "  public void m() { }" +
    	   	  "}"));
    }

    public void test30() {
    	testSucc(
    		Program.fromClasses(
    	      "abstract class S { abstract void m(); }",
    	      "class A extends S { void m() { } }",
    		  "class B extends A { }"),
   		    Program.fromClasses(
    	      "abstract class S { abstract void m(); }",
    	      "abstract class A extends S { }",
   		      "class B extends A { void m() { } }"));
    }
    
    public void test31() {
    	// push down should fail, since the pushed method cannot override the same method as before
    	testFail(Program.fromCompilationUnits(
    		  new RawCU("Super.java", "package p; class Super { int m() { return 23; } }"),
    		  new RawCU("A.java",     "package p; public class A extends Super { int m() { return 42; } }"),
    		  new RawCU("C.java",     "package p; class C { int f() { A a = new q.B(); return a.m(); } }"),
    		  new RawCU("B.java",     "package q; public class B extends p.A { }"))); 
    }
    public void test31b() {
    	// push down should fail, since the pushed method cannot override the same method as before
    	testFail(Program.fromCompilationUnits(
    		  new RawCU("Super.java", "package p; class Super { int m() { return 23; } }"),
    		  new RawCU("A.java",     "package p; public class A extends Super { int m() { return 42; } }"),
    		  new RawCU("D.java",     "package p; public class D extends A { }"),
    		  new RawCU("C.java",     "package p; class C { int f() { A a = new q.B(); return a.m(); } }"),
    		  new RawCU("B.java",     "package q; public class B extends p.A { }"))); 
    }

    public void test32() {
    	testSucc(
    		Program.fromClasses(
    		  "class S {" +
    		  "  void k() { }" +
    		  "}",
    		  "class A extends S {" +
    		  "  void m() { super.k(); }" +
    		  "}",
    		  "class B extends A { }"),
   		    Program.fromClasses(
    	      "class S {" +
    	      "  void k() { }" +
    	      "}",
    	      "class A extends S { }",
    	      "class B extends A {" +
    	      "  void m() { super.k(); }" +
    	      "}"));
    }
    
    public void test33() {
    	testFail(
    		Program.fromClasses(
    		  "interface I { int m(); }",
    		  "class Super implements I { public int m() { return 23; } }",
    		  "class A extends Super {" +
    		  "  public int m() { return 42; }" +
    		  "  public static void main(String[] args) {" +
    		  "    I i = new A();" +
    		  "    System.out.println(i.m());" +
    		  "  }" +
    		  "}",
    		  "class B extends A { }"));
    }
    
    public void test34() {
    	testFail(Program.fromClasses(
    		  "class A { static void m() { } }",
    		  "class Outer {" +
    		  "  class B extends A { }" +
    		  "}"));
    }

    public void test35() {
    	testSucc(
        	Program.fromClasses(
        	  "class A { final void m() { } }",
        	  "class B extends A { }"),
        	Program.fromClasses(
        	  "class A { }",
        	  "class B extends A { final void m() { } }"));
    }
    
    public void test36() {
    	testSucc(Program.fromClasses(
    	           "class A { final void m() { } void f() { m(); } }",
    	           "class B extends A { }"),
    	         Program.fromClasses(
    	    	   "abstract class A { abstract void m(); void f() { m(); } }",
    	    	   "class B extends A { final void m() { } }"));
    }
    
    public void test37() {
    	testFail(Program.fromCompilationUnits(
    			 new RawCU("Super.java",
    			 "package p;" +
    			 "public class Super {" +
    			 "  int m() { return 23; }" +
    			 "  public static void main(String[] args) {" +
    			 "    System.out.println(((Super)new q.B()).m());" +
    			 "  }" +
    			 "}"),
    			 new RawCU("A.java",
    			 "package p;" +
    			 "public class A extends Super { int m() { return 42; } }"),
    			 new RawCU("B.java",
    			 "package q;" +
    			 "public class B extends p.A { }")));
    }
    
    public void test38() {
    	testFail(Program.fromClasses(
    			"class OuterSuper {" +
    			"    int f;" +
    			"    OuterSuper(int f) { this.f = f; }" +
    			"    class A {" +
    			"        int m() { return f; }" +
    			"    }" +
    			"}",
    			"class OuterSub extends OuterSuper {" +
    			"    OuterSub() { super(23); }" +
    			"    class B extends A {" +
    			"        B() {" +
    			"            new OuterSuper(42).super();" +
    			"        }" +
    			"    }" +
    			"}"));
    }
    
    public void test39() {
    	testSucc(Program.fromClasses(
    			"class A {" +
    			"  public long m() {" +
    			"    return k();" +
    			"  }" +
    			"  public long k() {" +
    			"    return 10;" +
    			"  }" +
    			"}",
    			"class B extends A {" +
    			"  public long k() {" +
    			"    return 20;" +
    			"  }" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}"),
    			Program.fromClasses(
    			"class A {" +
    			"  public long k() {" +
    			"    return 10;" +
    			"  }" +
    			"}",
    			"class B extends A {" +
    			"  public long m() {" +
    			"    return k();" +
    			"  }" +
    			"  public long k() {" +
    			"    return 20;" +
    			"  }" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}"));
    }
    
    public void test40() {
    	testSucc(Program.fromClasses(
    			"class A {" +
    			"  long m() { return A.this.k(); }" +
    			"  long k() { return 1; }" +
    			"}",
    			"class B extends A { }"),
    			Program.fromClasses(
    			"class A {" +
    			"  long k() { return 1; }" +
    			"}",
    			"class B extends A {" +
    			"  long m() { return this.k(); }" +
    			"}"));
    }
    
    public void test41() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("A.java",
    			"package p;" +
    			"public class A {" +
    			"  public int m() {" +
    			"    return new A().k();" +
    			"  }" +
    			"  protected int k() {" +
    			"    return 1;" +
    			"  }" +
    			"}"),
    			new RawCU("B.java",
    			"package q;" +
    			"import p.*;" +
    			"public class B extends A { }")),
		Program.fromCompilationUnits(
    			new RawCU("A.java",
    			"package p;" +
    			"public class A {" +
    			"  public int k() {" +
    			"    return 1;" +
    			"  }" +
    			"}"),
    			new RawCU("B.java",
    			"package q;" +
    			"import p.*;" +
    			"public class B extends A {" +
    			"  public int m() {" +
    			"    return new A().k();" +
    			"  }" +
    			"}")));
    }

    public void test42() {
    	testSucc(Program.fromClasses(
    			"class A {" +
    			"  long m() { return k(); }" +
    			"  long k() { return 1; }" +
    			"}",
    			"class B extends A {" +
    			"  long k() { return 2; }" +
    			"  long test() { return m(); }" +
				"}"),
    			Program.fromClasses(
    			"class A {" +
    			"  long k() { return 1; }" +
    			"}",
    			"class B extends A {" +
    			"  long k() { return 2; }" +
    			"  long m() { return k(); }" +
    			"  long test() { return m(); }" +
    			"}"));
    }    
    
    public void test43() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("A.java",
    			"package p;" +
    			"public class A {" +
    			"  public int m() {" +
    			"    return new A().k(2);" +
    			"  }" +
    			"  protected int k(int a) {" +
    			"    return 1;" +
    			"  }" +
    			"  public int k(long a) {" +
    			"    return 2;" +
    			"  }" +
    			"}"),
    			new RawCU("B.java",
    			"package q;" +
    			"import p.*;" +
    			"public class B extends A {" +
    			"  int test() {" +
    			"    return m();" +
    			"  }" +
    			"}")),
		Program.fromCompilationUnits(
    			new RawCU("A.java",
    			"package p;" +
    			"public class A {" +
    			"  public int k(int a) {" +
    			"    return 1;" +
    			"  }" +
    			"  public int k(long a) {" +
    			"    return 2;" +
    			"  }" +
    			"}"),
    			new RawCU("B.java",
    			"package q;" +
    			"import p.*;" +
    			"public class B extends A {" +
    			"  public int m() {" +
    			"    return new A().k(2);" +
    			"  }" +
    			"  int test() {" +
    			"    return m();" +
    			"  }" +
    			"}")));
    }
}
