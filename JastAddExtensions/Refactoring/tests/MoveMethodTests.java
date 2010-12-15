package tests;

import junit.framework.TestCase;
import AST.FieldDeclaration;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.SimpleSet;
import AST.TypeDecl;

public class MoveMethodTests extends TestCase {
	public MoveMethodTests(String name) {
		super(name);
	}
	
	public void testSucc(String tp_name, String sig, String fld, Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		FieldDeclaration fd = tp.findField(fld);
		assertNotNull(fd);
		try {
			md.doMoveTo(fd, false, false, true);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testSucc(String tp_name, String sig, Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.moveToFirstParameter();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void testFail(String tp_name, String sig, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		try {
			md.moveToFirstParameter();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testFail(String tp_name, String sig, String fld, Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		TypeDecl tp = in.findType(tp_name);
		assertNotNull(tp);
		SimpleSet s = tp.localMethodsSignature(sig);
		assertTrue(s.isSingleton());
		MethodDecl md = (MethodDecl)s.iterator().next();
		FieldDeclaration fd = tp.findField(fld);
		assertNotNull(fd);
		try {
			md.doMoveTo(fd, true, true /*false, false,*/, true);
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

	public void test0() {
		testFail("A", "m()", Program.fromClasses("class A { void m() { } }"));
	}

	public void test1() {
		testFail("A", "m(int)", Program.fromClasses("class A { void m(int x) { } }"));
	}

	public void test2() {
		testFail("A", "m(I)", 
			Program.fromClasses(
			"interface I { }",
			"class A { void m(I i) { } }"));
	}

	public void test3() {
		testFail("A", "m(B)",
			Program.fromClasses(
			"class A { static void m(B b) { } }",
			"class B { }"));
	}

	public void test4() {
		testFail("A", "m(B)",
			Program.fromClasses(
			"abstract class A { abstract void m(B b); }",
			"class B { }"));
	}
	
	public void test5() {
		testFail("String", "contentEquals(java.lang.StringBuffer)", 
				Program.fromClasses("class A { }"));
	}
	
	public void test6() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { void m(B b) { } }",
			"class B { int m(B b) { return 23; } }"),
			Program.fromClasses(
		    "class A { void m(B b) { b.m(this); } }",
		    "class B { int m(B b) { return 23; }" +
		    "          void m(A a) { } }"));
	}
	
	public void test7() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { void m(B b) { } }",
			"class B { }"),
			Program.fromClasses(
			"class A { void m(B b) { b.m(this); } }",
			"class B { void m(A a) { } }"));
	}
	
	public void test8() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  int m(B b) {" +
			"    return 23;" +
			"  }" +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A { int m(B b) { return b.m(this); } }",
			"class B {" +
			"  int m(A a) {" +
			"    return 23;" +
			"  }" +
			"}"));
	}
	
	public void test9() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { " +
			"  int x;" +
			"  int m(B b) {" +
			"    return x;" +
			"  } " +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A { int x; int m(B b) { return b.m(this); } }",
			"class B { int m(A a) { return a.x; } }"));
	}
	
	public void test10() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { A m(B b) { return this; } }",
			"class B { }"),
			Program.fromClasses(
			"class A { A m(B b) { return b.m(this); } }",
			"class B { A m(A a) { return a; } }"));
	}
	
	public void test11() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { B m(B b) { return b; } }",
			"class B { }"),
			Program.fromClasses(
			"class A { B m(B b) { return b.m(this); } }",
			"class B { B m(A a) { return this; } }"));
	}
	
	public void test12() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { " +
			"  int x;" +
			"  int m(B b) {" +
			"    return this.x;" +
			"  } " +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A { int x; int m(B b) { return b.m(this); } }",
			"class B { int m(A a) { return a.x; } }"));
	}
	
	public void test13() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { " +
			"  int x;" +
			"  int m(B b) {" +
			"    return A.this.x;" +
			"  } " +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A { int x; int m(B b) { return b.m(this); } }",
			"class B { int m(A a) { return a.x; } }"));
	}
	
	public void test14() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class Super { int x; }",
			"class A extends Super { int m(B b) { return super.x; } }",
			"class B { }"),
			Program.fromClasses(
			"class Super { int x; }",
			"class A extends Super { int m(B b) { return b.m(this); } }",
			"class B { int m(A a) { return ((Super)a).x; } }"));
	}
	
	public void test15() {
		testFail("A", "m(B)",
			Program.fromClasses(
			"class Super { int m(B b) { return 23; } }",
			"class A extends Super { int m(B b) { return super.m(b)+19; } }",
			"class B { }"));
	}
	
	public void test16() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  int f() { return 23; }" +
			"  int m(B b) { return f()+19; }" +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A {" +
			"  int f() { return 23; }" +
			"  int m(B b) { return b.m(this); }" +
			"}" +
			"class B {" +
			"  int m(A a) { return a.f()+19; }" +
			"}"));
	}
	
	public void test17() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  int f() { return 23; }" +
			"  int m(B b) { return this.f()+19; }" +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A {" +
			"  int f() { return 23; }" +
			"  int m(B b) { return b.m(this); }" +
			"}" +
			"class B {" +
			"  int m(A a) { return a.f()+19; }" +
			"}"));
	}
	
	public void test18() {
		testSucc("A", "m(Outer.B)",
			Program.fromClasses(
			"class Outer {" +
			"  static int m() { return 23; }" +
			"  static class B {" +
			"    class Inner {" +
			"      int f() { return m(); }" +
			"    }" +
			"  }" +
			"}",
			"class A {" +
			"  int m(Outer.B b) {" +
			"    return 42;" +
			"  }" +
			"}"),
			Program.fromClasses(
			"class Outer {" +
			"  static int m() { return 23; }" +
			"  static class B {" +
			"    class Inner {" +
			"      int f() { return Outer.m(); }" +
			"    }" +
			"    int m(A a) {" +
			"      return 42;" +
			"    }" +
			"  }" +
			"}",
			"class A {" +
			"  int m(Outer.B b) {" +
			"    return b.m(this);" +
			"  }" +
			"}"));
	}
	
	public void test19() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class Outer {" +
			"  int x;" +
			"  class A {" +
			"    int y;" +
			"    int m(B b) {" +
			"      return x+y;" +
			"    }" +
			"  }" +
			"}" +
			"class B { }"),
			Program.fromClasses(
			"class Outer {" +
			"  int x;" +
			"  class A {" +
			"    int y;" +
			"    int m(B b) {" +
			"      return b.m(Outer.this, this);" +
			"    }" +
			"  }" +
			"}" +
			"class B {" +
			"  int m(Outer outer, Outer.A a) {" +
			"    return outer.x + a.y;" +
			"  }" +
			"}"));
	}
	
	public void test20() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  void m(B b) {" +
			"    new A() {" +
			"      Object f() {" +
			"        return this;" +
			"      }" +
			"    };" +
			"  }" +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A {" +
			"  void m(B b) {" +
			"    b.m(this);" +
			"  }" +
			"}",
			"class B {" +
			"  void m(A a) {" +
			"    new A() {" +
			"      Object f() {" +
			"        return this;" +
			"      }" +
			"    };" +
			"  }" +
			"}"));
	}
	
	public void test21() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  A m(B b) {" +
			"    new A() {" +
			"      Object f() {" +
			"        return this;" +
			"      }" +
			"    };" +
			"    return this;" +
			"  }" +
			"}",
			"class B { }"),
			Program.fromClasses(
			"class A {" +
			"  A m(B b) {" +
			"    return b.m(this);" +
			"  }" +
			"}",
			"class B {" +
			"  A m(A a) {" +
			"    new A() {" +
			"      Object f() {" +
			"        return this;" +
			"      }" +
			"    };" +
			"    return a;" +
			"  }" +
			"}"));
	}
	
	public void test22() {
		testSucc("A", "m(B, int)",
			Program.fromClasses(
			"class A { void m(B b, int a) { } }",
			"class B { }"),
			Program.fromClasses(
			"class A { void m(B b, int a) { b.m(this, a); } }",
			"class B { void m(A a0, int a) { } }"));
	}
	
	public void test23() {
		testSucc("A", "foo(B, A.Inner)",
			Program.fromClasses(
			"class B { class Inner { } }",
			"abstract class A {" +
			"  void foo(B f, Inner i) {" +
			"    new Inner();" +
			"  }" +
			"  class Inner { }" +
			"  void bar() {" +
			"    foo(new B(), new Inner());" +
			"  }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  class Inner {}" +
			"  void foo(A a, A.Inner i) {" +
			"    a.new Inner();" +
			"  }" +
			"}" +
			"abstract class A {" +
			"  void foo(B f, Inner i) {" +
			"    f.foo(this, i);" +
			"  }" +
			"  class Inner {}" +
			"  void bar() {" +
			"    foo(new B(), new Inner());" +
			"  }" +
			"}"));
	}
	
	public void test24() {
		testSucc("Inner", "foo(B)",
			Program.fromClasses(
			"class B { }",
			"class A {" +
			"  void bar(Inner inner) {}" +
			"  class Inner {" +
			"    void foo(final B b) {" +
			"      bar(this);" +
			"    }" +
			"  }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  void foo(A a, A.Inner inner) {" +
			"    a.bar(inner);" +
			"  }" +
			"}",
			"class A {" +
			"  void bar(Inner inner) {}" +
			"  class Inner {" +
			"    void foo(final B b) {" +
			"      b.foo(A.this, this);" +
			"    }" +
			"  }" +
			"}"));
	}
	
	public void test25() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class B { }",
			"class A {" +
			"  int x;" +
			"  void m(B b) { x++; }" +
			"  void bar() { m(new B()); }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  void m(A a) { " +
			"    a.x++;" +
			"  }" +
			"}",
			"class A {" +
			"  int x;" +
			"  void m(B b) { b.m(this); }" +
			"  void bar() { m(new B()); }" +
			"}"));
	}
	
	public void test26() {
		testFail("A", "m(A)",
			Program.fromClasses(
			"public class A {" +
			"  void m(A a) {" +
			"    m(a);" +
			"    a.m(a);" +
			"  }" +
			"}"));
	}
	
	public void test27() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class B {" +
			"  B g() { return null; }" +
			"}",
			"class A {" +
			"  void m(B b) {" +
			"    if(b != null) m(b.g());" +
			"  }" +
			"  void n() {" +
			"    m(new B());" +
			"  }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  B g() { return null; }" +
			"  void m(A a) {" +
			"    if(this != null)" +
			"      a.m(this.g());" +
			"  }" +
			"}",
			"class A {" +
			"  void m(B b) {" +
			"    b.m(this);" +
			"  }" +
			"  void n() {" +
			"    m(new B());" +
			"  }" +
			"}"));
	}
	
	public void test28() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class B { }",
			"class A {" +
			"  void m(B b) {" +
			"    notify();" +
			"  }" +
			"  void g() { }" +
			"  B getB() { return null; }" +
			"}",
			"class U {" +
			"  A myA;" +
			"  { myA.m(myA.getB()); }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  void m(A a) {" +
			"    a.notify();" +
			"  }" +
			"}",
			"class A {" +
			"  void m(B b) {" +
			"    b.m(this);" +
			"  }" +
			"  void g() { }" +
			"  B getB() { return null; }" +
			"}",
			"class U {" +
			"  A myA;" +
			"  { myA.m(myA.getB()); }" +
			"}"));
	}
	
	public void test29() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class B { }",
			"class A {" +
			"  void n() { }" +
			"  void m(final B b) {" +
			"    class Inner {" +
			"      { A.this.n(); }" +
			"    }" +
			"  }" +
			"}"),
			Program.fromClasses(
			"class B {" +
			"  void m(final A a) {" +
			"    class Inner {" +
			"      { a.n(); }" +
			"    }" +
			"  }" +
			"}",
			"class A {" +
			"  void n() { }" +
			"  void m(final B b) {" +
			"    b.m(this);" +
			"  }" +
			"}"));
	}
	
	public void test30() {
		testSucc("A", "m(B)",
			Program.fromClasses(
			"class A { synchronized void m(B b) { } }",
			"class B { }"),
			Program.fromClasses(
			"class A { synchronized void m(B b) { b.m(this); } }",
			"class B { void m(A a) { } }"));
	}
	
	public void test31() {
		testSucc("A", "mA1(B)",
			Program.fromClasses(
			"class A {" +
			"  public void mA1(B b) {" +
			"    b.mB1();" +
			"    mA2();" +
			"    b.mB2();" +
			"    System.out.println(this);" +
			"  }" +
			"  public void mA2() { }" +
			"}",
			"class B {" +
			"  public void mB1() { }" +
			"  public void mB2() { }" +
			"}",
			"class C {" +
			"  C() {" +
			"    getA().mA1(getB());" +
			"  }" +
			"  A getA() { return null; }" +
			"  B getB() { return null; }" +
			"}"),
			Program.fromClasses(
			"class A {" +
			"  public void mA1(B b) {" +
			"    b.mA1(this);" +
			"  }" +
			"  public void mA2() { }" +
			"}",
			"class B {" +
			"  public void mB1() { }" +
			"  public void mB2() { }" +
			"  public void mA1(A a) {" +
			"    this.mB1();" +
			"    a.mA2();" +
			"    this.mB2();" +
			"    System.out.println(a);" +
			"  }" +
			"}",
			"class C {" +
			"  C() {" +
			"    getA().mA1(getB());" +
			"  }" +
			"  A getA() { return null; }" +
			"  B getB() { return null; }" +
			"}"));
	}
	
	public void test32() {
		testFail("A", "m(B)",
			Program.fromClasses(
			"class A {" +
			"  private int k() { return 23; }" +
			"  int m(B b) { return k() + b.l(); }" +
			"}",
			"class B {" +
			"  int l() { return 42; }" +
			"}"));
	}
	
	public void test33() {
		testFail("A", "m(int)", "f",
 			Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"import q.*;" +
				"public class A extends B {" +
				"  public B f = null;" +
				"  public long m(int a) { return 0; }" +
				"  public long test() {" +
				"    return new A().m(2);" +
				"  }" +
				"}"),
			new RawCU("B.java",
				"package q;" +
				"public class B { }"),
			new RawCU("C.java",
				"package p;" +
				"import q.*;" +
				"public class C extends B {" +
				"  protected long m(int a) { return 2; }" +
				"}")));
	}
	
	public void test34() {
		testSucc("A", "m(int)", "f",
 			Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"import q.*;" +
				"public class A {" +
				"  public B f = null;" +
				"  public long m(int a) { return 0; }" +
				"}"),
			new RawCU("B.java",
				"package q;" +
				"public class B {" +
				"  protected long m(int a) { return 2; }" +
				"}")),
 			Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"import q.*;" +
				"public class A {" +
				"  public B f = null;" +
				"  public long m(int a) { return f.m(this, a); }" +
				"}"),
			new RawCU("B.java",
				"package q;" +
				"public class B {" +
				"  protected long m(int a) { return 2; }" +
				"  public long m(p.A a0, int a) { return 0; }" +
				"}")));
	}
	
	public void test35() {
		testFail("A", "m()", "f",
 			Program.fromCompilationUnits(
			new RawCU("A.java",
				"package p;" +
				"import q.*;" +
				"public class A {" +
				"  public B f = null;" +
				"  public long m() { return 0; }" +
				"}"),
			new RawCU("B.java",
				"package q;" +
				"public class B {" +
				"}"),
			new RawCU("C.java",
				"package p;" +
				"import q.*;" +
				"public class C extends B {" +
				"  long m() { return 1; }" +
				"}")));
	}
	
	public void test36() {
		testFail("B", "m()", "f",
 			Program.fromClasses(
			"public class A {" +
			"  public long m() { return 1; }" +
			"}",
			"public class B extends A {" +
			"  public C f = null;" +
			"  public long m() {" +
			"    return 0;" +
			"  }" +
			"}",
			"public class C { }",
			"class Test {" +
			"  { A a = new B(); a.m(); }" +
			"}"));
	}
}
