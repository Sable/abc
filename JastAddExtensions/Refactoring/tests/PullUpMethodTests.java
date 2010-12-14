package tests;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class PullUpMethodTests extends TestCase {
	public PullUpMethodTests(String name) {
		super(name);
	}
		
	public void testSucc(Program in, Program out, boolean withRequired) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			if(withRequired)
				md.doPullUpWithRequired();
			else
				md.doPullUp();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			assertEquals(out.toString(), rfe.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testSucc(Program in, Program out) {
		testSucc(in, out, false);
	}

	public void testFail(Program in, boolean withRequired) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			if(withRequired)
				md.doPullUpWithRequired();
			else
				md.doPullUp();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testFail(Program in) {
		testFail(in, false);
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

    public void test21() {
       	testSucc(
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {}"),
    				new RawCU("A.java","package q; public class A extends p.Super {static int i; static void m(){i++;}}")),
    				
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {static void m(){q.A.i++;}}"),
    				new RawCU("A.java","package q; public class A extends p.Super {public static int i;}")));
    }

    public void test22() {
    	testSucc(
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {}"),
    				new RawCU("A.java","package q; public class A extends p.Super {static void m(){} void n(){m();}}"),
    				new RawCU("Sub.java","package q; public class Sub extends A {static void m(){}}")),
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {protected static void m(){} }"),
    				new RawCU("A.java","package q; public class A extends p.Super {void n(){m();}}"),
    				new RawCU("Sub.java","package q; public class Sub extends A {protected static void m(){}}")));
    }

    public void test23() {
    	testSucc(
    			Program.fromCompilationUnits(
    				new RawCU("SuperSuper.java","package p; public class SuperSuper {static void m(){}}"),
    				new RawCU("Super.java","package q; public class Super extends p.SuperSuper {}"),
    				new RawCU("A.java","package p; public class A extends q.Super {static void m(){} void n(){m();}}")),
    			Program.fromCompilationUnits(
        				new RawCU("SuperSuper.java","package p; public class SuperSuper {static void m(){}}"),
        				new RawCU("Super.java","package q; public class Super extends p.SuperSuper {protected static void m(){}}"),
        				new RawCU("A.java","package p; public class A extends q.Super {void n(){m();}}")));
    }
    
    /* disabled for now (we now use a cheaper test to check whether the pulled-up method could be called dynamically,
     * which is fooled by these two cases)
    public void test24() {
    	testSucc(
    			Program.fromClasses(
    			"interface I { void m(); }",
    			"abstract class Super implements I {" +
    			"  void n() {" +
    			"    m();" +
    			"  }" +
    			"}",
    			"class A extends Super {" +
    			"  public void m() { }" +
    			"}"),
    			Program.fromClasses(
    			"interface I { void m(); }",
    			"abstract class Super implements I {" +
    			"  void n() {" +
    			"    m();" +
    			"  }" +
    			"  public void m() { }" +
    			"}",
    			"class A extends Super { }"));
    }
    
    public void test25() {
    	testSucc(
    			Program.fromClasses(
    			"interface I { void m(); }",
    			"abstract class SuperSuper implements I { }",
    			"abstract class Super extends SuperSuper {" +
    			"  void n() {" +
    			"    m();" +
    			"  }" +
    			"}",
    			"class A extends Super {" +
    			"  public void m() { }" +
    			"}"),
    			Program.fromClasses(
    			"interface I { void m(); }",
    			"abstract class SuperSuper implements I { }",
    			"abstract class Super extends SuperSuper {" +
    			"  void n() {" +
    			"    m();" +
    			"  }" +
    			"  public void m() { }" +
    			"}",
    			"class A extends Super { }"));
    }*/
    
    public void test26() {
    	testSucc(
    			Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  private static int m() { return 23; }" +
    			"  int n() { return m()+19; }" +
    			"}",
    			"class B extends Super {" +
    			"  private static int m() { return 56; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  static int m() { return 23; }" +
    			"}",
    			"class A extends Super {" +
    			"  int n() { return m()+19; }" +
    			"}",
    			"class B extends Super {" +
    			"  static int m() { return 56; }" +
    			"}"));
    			
    }
    
    public void test27() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  private static A m() { return null; }" +
    			"}",
    			"class B extends Super {" +
    			"  private static B m() { return null; }" +
    			"}"));
    }
    	
    public void test28() {
    	testSucc(
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {}"),
    				new RawCU("A.java","package q; public class A extends p.Super {static void m(){}}"),
    				new RawCU("Sub.java","package q; public class Sub extends A {static void m(){}}")),
    			Program.fromCompilationUnits(
    				new RawCU("Super.java","package p; public class Super {static void m(){} }"),
    				new RawCU("A.java","package q; public class A extends p.Super {}"),
    				new RawCU("Sub.java","package q; public class Sub extends A {static void m(){}}")));
    }
    
    public void test29() {
    	testFail(Program.fromClasses(
    			"class SuperSuper { Object m() { return null; } }",
    			"class Super extends SuperSuper { }",
    			"class A extends Super { String m() { return \"\"; } }",
    			"class B extends Super { Object m() { return null; } }"));
    }
    
    public void test30() {
    	testFail(Program.fromClasses(
    			"abstract class SuperSuper<T> { abstract T m(); }",
    			"abstract class Super<T> extends SuperSuper<T> { }",
    			"class A extends Super<String> { String m() { return \"23\"; } }"));
    }
    
    public void test31() {
    	testFail(Program.fromClasses(
    			"class Super {" +
    			"  int n(Super s) { return 23; }" +
    			"  int n(A a) { return 42; }" +
    			"}",
    			"class A extends Super {" +
    			"  int m() { return n(this); }" +
    			"}"));
    }
    
    public void test32() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class Outer {" +
    			"  int x;" +
    			"  class A extends Super {" +
    			"    int m() { return Outer.this.x; }" +
    			"  }" +
    			"}"));
    }
    
    public void test33() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class Outer {" +
    			"  int x;" +
    			"  class A extends Super {" +
    			"    Outer m() { return Outer.this; }" +
    			"  }" +
    			"}"));
    }
    
    public void test34() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class Outer {" +
    			"  class A extends Super {" +
    			"    Super m() { return this; }" +
    			"  }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  Super m() { return this; }" +
    			"}",
    			"class Outer {" +
    			"  class A extends Super { }" +
    			"}"));
    }
    
    public void test35() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  int m() { return 23; }" +
    			"}",
    			"class B extends Super {" +
    			"  String m() { return null; }" +
    			"}"));
    }
    
    public void test36() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  int m() { return 23; }" +
    			"}",
    			"class B extends Super {" +
    			"  void m() { }" +
    			"}"));
    }
    
    public void test37() {
    	testFail(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  void m() { }" +
    			"}",
    			"class B extends Super {" +
    			"  void m() throws java.io.IOException { }" +
    			"}"));
    }
    
    public void test38() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  void m() { }" +
    			"}",
    			"class B extends Super {" +
    			"  void m() throws java.util.NoSuchElementException { }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  void m() { }" +
    			"}",
    			"class A extends Super { }",
    			"class B extends Super {" +
    			"  void m() throws java.util.NoSuchElementException { }" +
    			"}"));
    }
    
    public void test39() {
    	testSucc(Program.fromCompilationUnits(
    			 new RawCU("Super.java",
    			   "package p;" +
    			   "public class Super { }"),
    			 new RawCU("B.java",
    			   "package q;" +
    			   "public @interface B { }"),
    			 new RawCU("A.java",
    			   "package q;" +
    			   "class A extends p.Super {" +
    			   "  @B void m() { }" +
    			   "}")),
    			 Program.fromCompilationUnits(
    			 new RawCU("Super.java",
    			   "package p;" +
    			   "public class Super {" +
    			   "  @q.B void m() { }" +
    			   "}"),
      			 new RawCU("B.java",
       			   "package q;" +
       			   "public @interface B { }"),
    			 new RawCU("A.java",
    			   "package q;" +
    			   "class A extends p.Super { }")));
    }
    
    public void test40() {
    	// an instance method may not override a static method
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super { static void m(){}}",
    			"class B extends Super { void m(){}}"));
    }
    
    public void test41() {
    	// can not override final method
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super { final void m(){}}",
    			"class B extends Super { void m(){}}"));
    }
   
    public void test42() {
       	// the return type does not match the return type and may thus not be overriden
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super { String[] m(){return null;}}",
    			"class B extends Super { String m(){return null;}}"));
    }
    
    public void test43() {
    	// a static method may not override an instance method
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class B extends Super { static void m(){}}",
    			"class A extends Super { void m(){}}"));
    }   
    
    public void test44() {
    	testFail(Program.fromClasses(
    			"class Super {}" +
    			"class A extends Super {" +
    			"  A a = this;" +
    			"  void m(){a.m();}" +
    			"}"), true);
    }
    
	public void test45() {
		testFail(Program.fromClasses("class Super {}" +
				"class A extends Super {" +
				"  class X { A n() {return A.this;}}" +
				"  void m(){X x;}" +
				"}"), true);
	}
    
	public void test46() {
		testFail(Program.fromClasses("class Super {}" +
				"class A extends Super {" +
				"  static class X { X m() {return this;}}" +
				"  void m(){X x;}" +
				"}"), true);
	}
    
    public void test47() {
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super {" +
    			"  A a = A.this;" +
    			"  void m(){a.m();}" +
    			"}"), true);
    }
    
    public void test48() {
    	testSucc(Program.fromCompilationUnits(
    		new RawCU("Super.java","package p; public class Super{}"),
    		new RawCU("A.java", 
    			"package q; " +
    			"public class A extends p.Super {" +
    			"  class X {void n(){}}" +
    			"  X x;" +
    			"  void m(X x){}" +
    			"}"),
    		new RawCU("B.java", "package q; " +
    			"class B extends A {" +
    			"  void k(){x.n();}" +
    			"}")),
    		Program.fromCompilationUnits(
    		new RawCU("Super.java",
    			"package p; " +
    			"public class Super{" +
    			"  protected class X {public void n(){}}" +
    			"  void m(X x){}" +
    	    	"}"),
    		new RawCU("A.java", 
    	    	"package q; " +
    	    	"public class A extends p.Super {" +
    	    	"  X x;" +
    	    	"}"),
   			new RawCU(
   				"B.java", "package q; " +
   		    	"class B extends A {" +
   		    	"  void k(){x.n();}" +
   		    	"}"))		
   				, true);
    }
    
    public void test49() {
    	testSucc(Program.fromClasses(
    			"class Super {" +
    			"	int n() {" +
    			"		return 42;" +
    			"	}" +
    			"}",
    			"class A extends Super {" +
    			"	int m() {" +
    			"		return this.n()+19;" +
    			"	}" +
    			"	int n() {" +
    			"		return 23;" +
    			"	}" +
    			"}",
    			"class Outer {" +
    			"	int m() { return 56; }" +
    			"	class B extends Super {" +
    			"		{ m(); }" +
    			"	}" +
    			"}",
    			"class C extends Super {" +
    			"	int m() { return 72; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"	int m() {" +
    			"		return this.n()+19;" +
    			"	}" +
    			"	int n() {" +
    			"		return 42;" +
    			"	}" +
    			"}",
    			"class A extends Super {" +
    			"	int n() {" +
    			"		return 23;" +
    			"	}" +
    	    	"}",
    			"class Outer {" +
    			"	int m() { return 56; }" +
    			"	class B extends Super {" +
    			"		{ Outer.this.m(); }" +
    			"	}" +
    			"}",
    			"class C extends Super {" +
    			"	int m() { return 72; }" +
    			"}"));
    }
    
    public void test50() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  class X {" +
    			"    private int v;" +
    			"  }" +
    			"  X x;" +
    			"  int m(X foo) { return x.v; }" +
    			"  int n() { return x.v+19; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  class X {" +
    			"    int v;" +
    			"  }" +
    			"  X x;" +
    			"  int m(X foo) { return x.v; }" +
    			"}",
    			"class A extends Super {" +
      			"  int n() { return x.v+19; }" +
       			"}"), true);    					
    }
    
    public void test51() {
    	testSucc(Program.fromClasses(
    			"class Super { }",
    			"class A extends Super {" +
    			"  class X {" +
    			"    class Y {" +
    			"      private int v;" +
    			"    }" +
    			"    Y y;" +
    			"  }" +
    			"  X x;" +
    			"  int m(X foo) { return x.y.v; }" +
    			"  int n() { return x.y.v+19; }" +
    			"}"),
    			Program.fromClasses(
    			"class Super {" +
    			"  class X {" +
    			"    class Y {" +
    			"      int v;" +
    			"    }" +
    			"    Y y;" +
    			"  }" +
    			"  X x;" +
    			"  int m(X foo) { return x.y.v; }" +
    			"}",
    			"class A extends Super {" +
      			"  int n() { return x.y.v+19; }" +
       			"}"), true);    					
    }
    
    public void test52() {
    	testSucc(Program.fromClasses("" +
    			"class Super {}",
    			"class A extends Super {" +
    			"  class X {private X(){}}" +
    			"  void m(X x){}" +
    			"  void n(){new X();}" +
    			"}"), 
    			Program.fromClasses("" +
    	    	"class Super {" +
    	    	"  class X {X(){}}" +
    	    	"  void m(X x){}" +
    	    	"}",
    	    	"class A extends Super {" +
    	    	"  void n(){new X();}" +
    	    	"}"), true);
    }
    
    public void test53() {
    	testSucc(Program.fromClasses("" +
    			"class Super {}",
    			"class A extends Super {" +
    			"  class X {" +
    			"    class Y{private Y(){}}" +
    			"  }" +
    			"  void m(X x){}" +
    			"  X x;" +
    			"  void n(){x.new Y();}" +
    			"}"), 
    			Program.fromClasses("" +
    	    	"class Super {" +
    	    	"  class X {" +
    	    	"    class Y{Y(){}}" +
    	    	"  }" +
    	    	"  void m(X x){}" +
    	    	"}",
    	    	"class A extends Super {" +
    	    	"  X x;" +
    	    	"  void n(){x.new Y();}" +
    	    	"}"), true);
    }
    
    public void test54() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("Super.java","package q; public class Super{}"),
    			new RawCU("A.java","package p; class A extends q.Super {" +
    					"  void m(X x){}" +
    					"  class X extends B{ void n(){}}" +
    					"}"),
    			new RawCU("B.java","package p; public abstract class B{abstract void n();}")),
    			Program.fromCompilationUnits(
    	    	new RawCU("Super.java","package q; public class Super{" +
    	    			"  void m(X x){}" +
    	    			"  class X extends p.B{ protected void n(){}}" +
    	    			"}"),
    	    	new RawCU("A.java","package p; class A extends q.Super {}"),
    	    	new RawCU("B.java","package p; public abstract class B{abstract protected void n();}")),true);
    }
    
    public void test55() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("Super.java","package q; public class Super{}"),
    			new RawCU("A.java","package p; class A extends q.Super {" +
    					"  void m(Y.X x){}" +
    					"  public class Y { class X extends B{ void n(){}}}" +
    					"}"),
    			new RawCU("B.java","package p; public abstract class B{abstract void n();}")),
    			Program.fromCompilationUnits(
    	    	new RawCU("Super.java","package q; public class Super{" +
    	    			"  void m(Y.X x){}" +
    	    			"  public class Y { class X extends p.B{ protected void n(){}}}" +
    	    			"}"),
    	    	new RawCU("A.java","package p; class A extends q.Super {}"),
    	    	new RawCU("B.java","package p; public abstract class B{abstract protected void n();}")),true);
    }
    
    public void test56() {
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super {Integer m(){return null;}}",
    			"class B extends Super {String m(){return null;}}"
    			), true);
    }

    public void test57() {
    	testFail(Program.fromClasses(
    			"class Super {}",
    			"class A extends Super {java.util.Set<Integer> m(){return null;}}",
    			"class B extends Super {java.util.Set<String>  m(){return null;}}"
    			), true);
    }
    
    public void test58() {
    	testFail(Program.fromClasses(
    			"class Super {" +
    			"  int k() {" +
    			"    return 10;" +
    			"  }" +
    			"}",
    			"class A extends Super {" +
    			"  int k() {" +
    			"    return 20;" +
    			"  }" +
    			"  int m() {" +
    			"    return super.k();" +
    			"  }" +
    			"  int test() {" +
    			"    return m();" +
    			"  }" +
    			"}"));
    }
    
    public void test59() {
    	testFail(Program.fromCompilationUnits(
    			new RawCU("Super.java",
    			"package p;" +
    			"public class Super { }"),
    			new RawCU("A.java",
    			"package q;" +
    			"import p.*;" +
    			"public class A extends Super {" +
    			"  public long m() {" +
    			"    return A.this.k(2);" +
    			"  }" +
    			"  public long k(long a) {" +
    			"    return 1;" +
    			"  }" +
    			"}"),
    			new RawCU("B.java",
    			"package r;" +
    			"public class B {" +
    			"  protected long k(long a) {" +
    			"    return 3;" +
    			"  }" +
    			"}")));
    }
    
    public void test60() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("SuperSuper.java",
    			"package p;" +
    			"public class SuperSuper {" +
    			"  protected long k(long a) {" +
    			"    return 3;" +
    			"  }" +
    			"}"),
    			new RawCU("Super.java",
    			"package q;" +
    			"import p.*;" +
    			"public class Super extends SuperSuper {" +
    			"}"),
    			new RawCU("A.java",
    			"package p;" +
    			"import q.*;" +
    			"public class A extends Super {" +
    			"  public long m() {" +
    			"    return new SuperSuper().k(2);" +
    			"  }" +
    			"  protected long k(int a) {" +
    			"    return 1;" +
    			"  }" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}")),
                 Program.fromCompilationUnits(
    			new RawCU("SuperSuper.java",
    			"package p;" +
    			"public class SuperSuper {" +
    			"  public long k(long a) {" +
    			"    return 3;" +
    			"  }" +
    			"}"),
    			new RawCU("Super.java",
    			"package q;" +
    			"import p.*;" +
    			"public class Super extends SuperSuper {" +
    			"  public long m() {" +
    			"    return new SuperSuper().k(2);" +
    			"  }" +
    			"}"),
    			new RawCU("A.java",
    			"package p;" +
    			"import q.*;" +
    			"public class A extends Super {" +
    			"  protected long k(int a) {" +
    			"    return 1;" +
    			"  }" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}")));
    }
    
    public void test61() {
    	testSucc(Program.fromCompilationUnits(
    			new RawCU("SuperSuper.java",
    			"package p;" +
    			"public class SuperSuper {" +
    			"  public long k(int a) {" +
    			"    return 10;" +
    			"  }" +
    			"  public long k(long a) {" +
    			"    return 20;" +
    			"  }" +
    			"}"),
    			new RawCU("Super.java",
    			"package q;" +
    			"import p.*;" +
    			"public class Super extends SuperSuper {" +
    			"}"),
    			new RawCU("A.java",
    			"package p;" +
    			"import q.*;" +
    			"public class A extends Super {" +
    			"  public long m() {" +
    			"    return new SuperSuper().k(2);" +
    			"  }" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}")),
                 Program.fromCompilationUnits(
    			new RawCU("SuperSuper.java",
    			"package p;" +
    			"public class SuperSuper {" +
    			"  public long k(int a) {" +
    			"    return 10;" +
    			"  }" +
    			"  public long k(long a) {" +
    			"    return 20;" +
    			"  }" +
    			"}"),
    			new RawCU("Super.java",
    			"package q;" +
    			"import p.*;" +
    			"public class Super extends SuperSuper {" +
    			"  public long m() {" +
    			"    return new SuperSuper().k(2);" +
    			"  }" +
    			"}"),
    			new RawCU("A.java",
    			"package p;" +
    			"import q.*;" +
    			"public class A extends Super {" +
    			"  public long test() {" +
    			"    return m();" +
    			"  }" +
    			"}")));
    }
}