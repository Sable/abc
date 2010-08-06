package tests;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class RenameMethodTests extends TestCase {
	public RenameMethodTests(String name) {
		super(name);
	}
	
	public void testSucc(String tp_name, String sig, String new_name, Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChanges();
		assertNotNull(out);
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.rename(new_name);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}
	
	public void testFail(String tp_name, String sig, String new_name, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChanges();
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.rename(new_name);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}
	
    public void test1() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses("class A { void m() { } }"),
    		Program.fromClasses("class A { void n() { } }"));    		
    }

    public void test2() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A {" +
    		"  void m() { }" +
    		"  float n() { return 1.0f; }" +
    		"}"));    		
    }

    public void test3() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A {" +
    		"  void m() { }" +
    		"  float n(char c) { return 1.0f; }" +
    		"}"),
    		Program.fromClasses(
      		"class A {" +
       		"  void n() { }" +
       		"  float n(char c) { return 1.0f; }" +
       		"}"));    		
    }

    public void test4() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { void m() { } }",
    		"class B { void m() { } }"),
    		Program.fromClasses(
    		"class A { void n() { } }",
    		"class B { void m() { } }"));
    }

    public void test5() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { void m() { } }",
    		"class B extends A { void m() { } }"),
    		Program.fromClasses(
    		"class A { void n() { } }",
    		"class B extends A { void n() { } }"));
    }
    
    public void test6() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z {" +
    		"  void m() { }" +
    		"  String m(int i) { return \"aluis\"; }" +
    		"}",
    		"class A extends Z {" +
    		"  void m() { }" +
    		"}",
    		"class B extends A {"+
    		"  void m() { }" +
    		"}"),
    		Program.fromClasses(
    	    "class Z {" +
    	    "  void n() { }" +
    	    "  String m(int i) { return \"aluis\"; }" +
    	    "}",
    	    "class A extends Z {" +
    	    "  void n() { }" +
    	    "}",
    	    "class B extends A {"+
    	    "  void n() { }" +
    	    "}"));
    }
    
    public void test7() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z {" +
    		"  void m() { }" +
    		"  String n() { return \"aluis\"; }" +
    		"}",
    		"class A extends Z {" +
    		"  void m() { }" +
    		"}",
    		"class B extends A {"+
    		"  void m() { }" +
    		"}"));
    }
    
    public void test8() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z {" +
    		"  void m() { }" +
    		"  String n(int i) { return \"aluis\"; }" +
    		"}",
    		"class A extends Z {" +
    		"  void m() { }" +
    		"}",
    		"class B extends A {"+
    		"  void m() { }" +
    		"}"),
    		Program.fromClasses(
    	    "class Z {" +
    	    "  void n() { }" +
    	    "  String n(int i) { return \"aluis\"; }" +
    	    "}",
    	    "class A extends Z {" +
    	    "  void n() { }" +
    	    "}",
    	    "class B extends A {"+
    	    "  void n() { }" +
    	    "}"));
    }
    
    public void test10() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { public void n() { } }",
    		"class A extends Z { void m() { n(); } }"));
    }
    
    public void test11() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses("class A { static void m() { } }"),
    		Program.fromClasses("class A { static void n() { } }"));    		
    }
    
    public void test12() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { static void n() { } }",
    		"class A extends Z { static void m() { } }",
    		"class B { void m() { A a = new A(); a.n(); } }"),
    		Program.fromClasses(
    	    "class Z { static void n() { } }",
    	    "class A extends Z { static void n() { } }",
    	    "class B { void m() { A a = new A(); ((Z)a).n(); } }"));
    }
    
    public void test13() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { void n() { } }",
    		"class A extends Z { void m() { } }",
    		"class B { void m() { A a = new A(); a.n(); } }"));
    }
    
    public void test15() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A {" +
    		"  void m() { }" +
    		"  class B {" +
    		"    void n() { }" +
    		"    void p() { m(); }" +
    		"  }" +
    		"}"),
    		Program.fromClasses(
    		"class A {" +
    		"  void n() { }" +
    		"  class B {" +
    		"    void n() { }" +
    		"    void p() { A.this.n(); }" +
    		"  }" +
    		"}"));
    }
    
    public void test16() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A {" +
    		"  void m() { }" +
    		"  class B {" +
    		"    void n() { }" +
    		"    void p() { A.this.m(); }" +
    		"  }" +
    		"}"),
    		Program.fromClasses(
    		"class A {" +
    		"  void n() { }" +
    		"  class B {" +
    		"    void n() { }" +
    		"    void p() { A.this.n(); }" +
    		"  }" +
    		"}"));
    }
    
    public void test17() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { void n() { } }",
    		"class A extends Z {" +
    		"  void m() { }" +
    		"  class B {" +
    		"    void n() { }" +
    		"    void p() { A.this.n(); }" +
    		"  }" +
    		"}"));
    }
    
    public void test19() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { static void n() { } }",
    		"class A extends Z {" +
    		"  static void m() { }" +
    		"  static { n(); }" +
    		"}"),
    		Program.fromClasses(
    		"class Z { static void n() { } }",
    		"class A extends Z {" +
    		"  static void n() { }" +
    		"  static { Z.n(); }" +
    		"}"));
    }
    
    public void test20() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { static void n() { } }",
    		"class A extends Z { static void m() { } }",
    		"class B extends A { { n(); } }"),
    		Program.fromClasses(
     		"class Z { static void n() { } }",
     		"class A extends Z { static void n() { } }",
     		"class B extends A { { Z.n(); } }"));
    }
    
    public void test21() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { void n() { } }",
    		"class A extends Z { void m() { } }",
    		"class B extends A { { n(); } }"));
    }
    
    public void test22() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A {" +
    		"  static int m() { return 23; }" +
    		"  static class B {" +
    		"    static void n(int i) { }" +
    		"    int k = m();" +
    		"  }" +
    		"}"),
    		Program.fromClasses(
    		"class A {" +
    		"  static int n() { return 23; }" +
    		"  static class B {" +
    		"    static void n(int i) { }" +
    		"    int k = A.n();" +
    		"  }" +
    		"}"));
    }
    
    public void test23() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class Z { static int n() { return 72; } }",
    		"class A extends Z { static int m() { return 23; } }",
    		"class B extends A { class C { int k = n(); } }"),
    		Program.fromClasses(
    		"class Z { static int n() { return 72; } }",
    		"class A extends Z { static int n() { return 23; } }",
    		"class B extends A { class C { int k = Z.n(); } }"));
    }
    
    public void test24() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { int m() { return 23; } }",
    		"class B { A a; int p() { return a.m(); } }"),
    		Program.fromClasses(
    		"class A { int n() { return 23; } }",
    		"class B { A a; int p() { return a.n(); } }"));
    }
    
    public void test27() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { boolean[] m() { return null; } }",
    		"class B extends A { void p() { m(); } }"),
    		Program.fromClasses(
    		"class A { boolean[] n() { return null; } }",
    		"class B extends A { void p() { n(); } }"));
    }
    
    public void test28() {
    	testSucc(
    		"A", "m(int)", "n",
    		Program.fromClasses(
    		"class Z { void n() { } }",
    		"class A {" +
    		"  boolean[] m(int i) { return null; }" +
    		"  class B extends Z {" +
    		"    void p() { m(42); }" +
    		"  }" +
    		"}"),
    		Program.fromClasses(
    	    "class Z { void n() { } }",
    	    "class A {" +
    	    "  boolean[] n(int i) { return null; }" +
    	    "  class B extends Z {" +
    	    "    void p() { A.this.n(42); }" +
    	    "  }" +
    	    "}"));
    }
    
    public void test29() {
    	testSucc(
    		"A", "m(int)", "valueOf",
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"import static java.lang.String.*;" +
    		"public class A {" +
    		"  static String m(int i) { return \"42\"; }" +
    		"  public static void main(String[] args) {" +
    		"    System.out.println(valueOf(23));" +
    		"  }" +
    		"}")),
    		Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "import static java.lang.String.*;" +
    	    "public class A {" +
    	    "  static String valueOf(int i) { return \"42\"; }" +
    	    "  public static void main(String[] args) {" +
    	    "    System.out.println(String.valueOf(23));" +
    	    "  }" +
    	    "}")));
    }
    
    public void test30() {
    	testSucc(
    		"A", "m(int)", "valueOf",
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"import static java.lang.String.*;" +
    		"public class A {" +
    		"  static String m(int i) { return \"42\"; }" +
    		"  public static void main(String[] args) {" +
    		"    System.out.println(String.valueOf(23));" +
    		"  }" +
    		"}")),
    		Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "import static java.lang.String.*;" +
    	    "public class A {" +
    	    "  static String valueOf(int i) { return \"42\"; }" +
    	    "  public static void main(String[] args) {" +
    	    "    System.out.println(String.valueOf(23));" +
    	    "  }" +
    	    "}")));
    }
    
    public void test31() {
    	testSucc(
    		"A", "m(int)", "valueOf",
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"import static java.lang.String.*;" +
    		"public class A {" +
    		"  static String m(int i) { return \"42\"; }" +
    		"  static int String;"+
    		"  public static void main(String[] args) {" +
    		"    System.out.println(valueOf(23));" +
    		"  }" +
    		"}")),
    		Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "import static java.lang.String.*;" +
    	    "public class A {" +
    	    "  static String valueOf(int i) { return \"42\"; }" +
    	    "  static int String;"+
    	    "  public static void main(String[] args) {" +
    	    "    System.out.println(java.lang.String.valueOf(23));" +
    	    "  }" +
    	    "}")));
    }
    
    public void test32() {
    	testFail(
    		"A", "m(int)", "valueOf",
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"import static java.lang.String.*;" +
    		"public class A {" +
    		"  static String m(int i) { return \"42\"; }" +
    		"  static int String;"+
    		"  static char java;"+
    		"  public static void main(String[] args) {" +
    		"    System.out.println(valueOf(23));" +
    		"  }" +
    		"}")));
    }
    
    public void test33() {
    	testSucc(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { public void m() { System.out.println(42); } }",
    		"class B {" +
    		"  static void n() { System.out.println(23); }" +
    		"  class C extends A { { n(); } }" +
    		"}"),
    		Program.fromClasses(
    		"class A { public void n() { System.out.println(42); } }",
    		"class B {" +
    		"  static void n() { System.out.println(23); }" +
    		"  class C extends A { { B.n(); } }" +
    	    "}"));    		
    }
    
    public void test34() {
    	testSucc(
    		"B", "m()", "n",
    		Program.fromCompilationUnits(
    		new RawCU("A.java",
    		"package p;" +
    		"public class A { void m() { } }"),
    		new RawCU("B.java",
    		"package q;" +
    		"public class B extends p.A { void m() { } }")),
    		Program.fromCompilationUnits(
    	    new RawCU("A.java",
    	    "package p;" +
    	    "public class A { void m() { } }"),
    	    new RawCU("B.java",
    	    "package q;" +
    	    "public class B extends p.A { void n() { } }")));
    }
    
    public void test36() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { static String m() { return \"hello\"; } }",
    		"class C {" +
    		"  static class B extends A {" +
    		"    static String n() { return \"world!\"; }" +
    		"  }" +
    		"  static { System.out.println(C.B.m().length()); }" +
    		"}"));
    }
    
    public void test37() {
    	testFail(
    		"A.D", "g()", "f",
    		Program.fromClasses(
    		"class A {" +
    		"  private static class C {" +
    		"    static int f() { return 23; }" +
    		"  }" +
    		"  static class D extends C {" +
    		"    static int g() { return 42; }" +
    		"  }" +
    		"}",
    		"class B extends A {" +
    		"  { new D().f(); }" +
    		"}"));
    }
    
    public void test38() {
    	testSucc(
    		"A", "f(int)", "g",
    		Program.fromClasses(
    		"class A {" +
    		"  void g(long x) { System.out.println(x); }" +
    		"  void f(int x)  { System.out.println(x+19); }" +
    		"  { g(23); }" +
    		"}"),
    		Program.fromClasses(
    		"class A {" +
    		"  void g(long x) { System.out.println(x); }" +
    		"  void g(int x)  { System.out.println(x+19); }" +
    		"  { g((long)23); }" +
    		"}"));
	}
    
    public void test39() {
       	testSucc(
       		"A", "m()", "n",
       		Program.fromClasses(
			"class Super { void m() { } }",
			"class A extends Super { void m() { } }",
       		"class B extends Super { void m() { } }"),
       		Program.fromClasses(
			"class Super { void n() { } }",
			"class A extends Super { void n() { } }",
       		"class B extends Super { void n() { } }"));       		    		
    }
    
    public void test40() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { void m() { } }",
    		"class B extends A { void n() { } }"));
    }
    
    
    public void test41Fail() {
    	testFail(
    		"B", "m()", "n",
    		Program.fromClasses(
    		"class A { void m() { } }",
    		"abstract class B extends A implements I { void m() { } }",
    		"interface I { void n(); }"));
    }
    
    public void test42Fail() {
    	testFail(
    		"A", "m()", "n",
    		Program.fromClasses(
    		"class A { void m() { } }",
    		"abstract class B extends A implements I { }",
    		"interface I { void n(); }"));
    }
    
}