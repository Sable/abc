package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.BodyDecl;
import AST.MemberTypeDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class MoveMemberTypeToToplevelTests extends TestCase {
	public MoveMemberTypeToToplevelTests(String name) {
		super(name);
	}
	
	MemberTypeDecl findMemberType(Program in) {
		TypeDecl td = in.findSimpleType("A");
		assertNotNull(td);

		for(BodyDecl bd : td.getBodyDecls())
			if(bd.declaresType("B"))
				return (MemberTypeDecl)bd;
		fail("member type not found");
		return null;
	}
	
	public void testSucc(Program in, Program out) {
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		try {
			findMemberType(in).moveToToplevel();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException e) {
			assertEquals(out.toString(), e.toString());
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void testFail(Program in) {
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		try {
			findMemberType(in).moveToToplevel();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException e) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void test1() {
		testSucc(Program.fromClasses("class A { class B { } }"),
				 Program.fromClasses("class A { }",
						             "class B { private final A a; " +
						             "          B(A a0) { this.a = a0; } }"));
	}
	
	public void test2() {
		testSucc(Program.fromClasses("class Outer { class A { class B { } } }"),
				 Program.fromClasses("class Outer { class A { } }",
						             "class B { private final Outer.A a; " +
						             "          private final Outer outer; " +
						             "          B(Outer.A a0, Outer outer0) {" +
						             "            this.a = a0;" +
						             "            this.outer = outer0;" +
						             "          } }"));
	}
	
	public void test3() {
		testSucc(Program.fromClasses("class A { int x; class B { int m() { return x; } } }"),
				 Program.fromClasses("class A { int x; }",
						             "class B { private final A a;" +
						             "          int m() { return a.x; } " +
						             "          B(A a0) { this.a = a0; } }"));
	}
	
	public void test4() {
		testFail(Program.fromClasses("class A { private int x; class B { int m() { return x; } } }"));
	}
	
	public void test5() {
		testSucc(Program.fromClasses("class A { static class B { } }"),
				 Program.fromClasses("class A { }",
						             "class B { }"));
	}
	
	public void test6() {
		testSucc(Program.fromClasses("class A { interface B { } }"),
				 Program.fromClasses("class A { }",
						             "interface B { }"));
	}
	
	public void test7() {
		testSucc(Program.fromClasses("class A { private class B { } }"),
				 Program.fromClasses("class A { }",
						             "class B { private final A a; " +
						             "          private B(A a0) { this.a = a0; } }"));
	}
	
	public void test8() {
		testSucc(Program.fromClasses("class A { class B { int x; B(int x) { this.x = x; } } }"),
				 Program.fromClasses("class A { }",
						             "class B { private final A a; " +
						             "          int x;" +
						             "          B(A a0, int x) { this.a = a0; this.x = x; } }"));
	}
	
	public void test9() {
		testSucc(Program.fromClasses("class A { class B { int x; B(int x) { this.x = x; } B() { this(23); } } }"),
				 Program.fromClasses("class A { }",
						             "class B { private final A a; " +
						             "          int x;" +
						             "          B(A a0, int x) { this.a = a0; this.x = x; } " +
						             "          B(A a) { this(a, 23); } }"));
	}
	
	public void test10() {
		testSucc(Program.fromClasses(
					"class Outer {" +
					"	int x;" +
					"	class A {" +
					"		int y;" +
					"		class B {" + 
					"			int m() {" +
					"				return x+y;" +
					"			}" +
					"		}" +
					"	}" +
					"}"),
				 Program.fromClasses(
					"class Outer {" +
					"	int x;" +
					"	class A {" +
					"		int y;" +
					"	}" +
				    "}" +
				    "class B {" +
				    "  private final Outer.A a;" +
				    "  private final Outer outer;" +
				    "  int m() {" +
				    "    return outer.x + a.y;" +
				    "  }" +
				    "  B(Outer.A a0, Outer outer0) {" +
				    "    this.a = a0;" +
				    "    this.outer = outer0;" +
				    "  }" +
				    "}"));
	}
	
	public void test11() {
		testSucc(Program.fromClasses(
					"class Outer {" +
					"	int x;" +
					"	class A {" +
					"		int y;" +
					"		class B {" + 
					"			int m() {" +
					"				return x+y;" +
					"			}" +
					"		}" +
					"		B b = new B();" +
					"	}" +
					"}"),
				 Program.fromClasses(
					"class Outer {" +
					"	int x;" +
					"	class A {" +
					"		int y;" +
					"       B b = new B(this, Outer.this);" +
					"	}" +
				    "}" +
				    "class B {" +
				    "  private final Outer.A a;" +
				    "  private final Outer outer;" +
				    "  int m() {" +
				    "    return outer.x + a.y;" +
				    "  }" +
				    "  B(Outer.A a0, Outer outer0) {" +
				    "    this.a = a0;" +
				    "    this.outer = outer0;" +
				    "  }" +
				    "}"));
	}
	
	public void test12() {
		testFail(Program.fromClasses(
					"class Outer {" +
					"	int x;" +
					"	class A {" +
					"		int y;" +
					"		class B {" + 
					"			int m() {" +
					"				return x+y;" +
					"			}" +
					"		}" +
					"		B m(A a) {" +
					"           return a.new B();" +
					"       }" +
					"	}" +
					"}"));
	}
	
	public void test13() {
		testSucc(Program.fromClasses(
				   "class A {" +
				   "    class B { } " +
				   "    B m(A a) { return a.new B(); }" +
				   "}"),
				 Program.fromClasses(
				   "class A { " +
				   "    B m(A a) { return new B(a); }" +
				   "}",
				   "class B {" +
				   "    private final A a; " +
				   "    B(A a0) { this.a = a0; }" +
				   "}"));
	}
	
	public void test14() {
		testSucc(Program.fromClasses(
				   "class A {" +
				   "  class B {" +
				   "  }" +
				   "}",
				   "class C extends A.B {" +
				   "  C(A a) {" +
				   "    a.super();" +
				   "  }" +
				   "}"),
				 Program.fromClasses(
				   "class A { }",
				   "class B {" +
				   "  private final A a;" +
				   "  B(A a0) {" +
				   "    this.a = a0;" +
				   "  }" +
				   "}",
				   "class C extends B {" +
				   "  C(A a) {" +
				   "    super(a);" +
				   "  }" +
				   "}"));
	}
	
	public void test15() {
		testSucc(Program.fromClasses(
				   "class A {" +
				   "  class B { }" +
				   "  class C extends B { }" +
				   "}"),
				 Program.fromClasses(
				   "class A { " +
				   "  class C extends B {" +
				   "    C() {" +
				   "      super(A.this);" +
				   "    }" +
				   "  }" +
				   "}",
				   "class B {" +
				   "  private final A a;" +
				   "  B(A a0) {" +
				   "    this.a = a0;" +
				   "  }" +
				   "}"));
	}

	public void test16() {
		testSucc(Program.fromClasses(
				   "class A {" +
				   "  class B {" +
				   "  }" +
				   "}",
				   "class D extends A { }",
				   "class C extends A.B {" +
				   "  C(D d) {" +
				   "    d.super();" +
				   "  }" +
				   "}"),
				 Program.fromClasses(
				   "class A { }",
				   "class D extends A { }",
				   "class B {" +
				   "  private final A a;" +
				   "  B(A a0) {" +
				   "    this.a = a0;" +
				   "  }" +
				   "}",
				   "class C extends B {" +
				   "  C(D d) {" +
				   "    super(d);" +
				   "  }" +
				   "}"));
	}
	
	public void test17() {
		testSucc(Program.fromClasses(
					"class A {" +
					"  int y;" +
					"  class B {" + 
					"    int m() {" +
					"	   return y;" +
					"	 }" +
					"  }" +
					"}",
					"class C {" +
					"  A.B b = new A().new B();" +
					"}"),
				  Program.fromClasses(
				    "class A {" +
				    "  int y;" +
				    "}" +
				    "class B {" +
				    "  private final A a;" +
				    "  int m() {" +
				    "    return a.y;" +
				    "  }" +
				    "  B(A a0) {" +
				    "    this.a = a0;" +
				    "  }" + 
				    "}" +
				    "class C {" +
				    "  B b = new B(new A());" +
				    "}"));
	}
	
	public void test18() {
		testSucc(Program.fromClasses(
					"class A {" +
					"  int y;" +
					"  class B {" + 
					"    int m() {" +
					"	   return y;" +
					"	 }" +
					"  }" +
					"}",
					"class D {" +
					"  A a;" +
					"}",
					"class C {" +
					"  Object m(D d) {" +
					"    return d.a.new B();" +
					"  }" +
					"}"),
				  Program.fromClasses(
				    "class A {" +
				    "  int y;" +
				    "}",
				    "class B {" +
				    "  private final  A a;" +
				    "  int m() {" +
				    "    return a.y;" +
				    "  }" +
				    "  B(A a0) {" +
				    "    this.a = a0;" +
				    "  }" + 
				    "}",
				    "class D {" +
				    "  A a;" +
				    "}",
				    "class C {" +
				    "  Object m(D d) {" +
				    "    return new B(d.a);" +
				    "  }" +
				    "}"));
	}
	
	public void test19() {
		testSucc(Program.fromClasses(
					"class A {" +
					"  int y;" +
					"  class B {" + 
					"    int m() {" +
					"	   return y;" +
					"	 }" +
					"  }" +
					"}",
					"class D {" +
					"  A a;" +
					"}",
					"class C extends A.B {" +
					"  C(D d) {" +
					"    d.a.super();" +
					"  }" +
					"}"),
				  Program.fromClasses(
				    "class A {" +
				    "  int y;" +
				    "}",
				    "class B {" +
				    "  private final A a;" +
				    "  int m() {" +
				    "    return a.y;" +
				    "  }" +
				    "  B(A a0) {" +
				    "    this.a = a0;" +
				    "  }" + 
				    "}",
				    "class D {" +
				    "  A a;" +
				    "}",
				    "class C extends B {" +
				    "  C(D d) {" +
				    "    super(d.a);" +
				    "  }" +
				    "}"));
	}
	
	public void test20() {
		testSucc(Program.fromClasses(
					"class A {" +
					"  int y;" +
					"  class B {" + 
					"    int m() {" +
					"	   return y;" +
					"	 }" +
					"  }" +
					"}",
					"class D {" +
					"  A a;" +
					"}",
					"class C {" +
					"  int m(D d) {" +
					"    return d.a.new B().m();" +
					"  }" +
					"}"),
				  Program.fromClasses(
				    "class A {" +
				    "  int y;" +
				    "}",
				    "class B {" +
				    "  private final A a;" +
				    "  int m() {" +
				    "    return a.y;" +
				    "  }" +
				    "  B(A a0) {" +
				    "    this.a = a0;" +
				    "  }" + 
				    "}",
				    "class D {" +
				    "  A a;" +
				    "}",
				    "class C {" +
				    "  int m(D d) {" +
				    "    return new B(d.a).m();" +
				    "  }" +
				    "}"));
	}
	
	public void test21() {
		testSucc(Program.fromClasses("class A { class B { private int x; int getX() { return x; } } }"),
				 Program.fromClasses("class A { }",
						             "class B { private final A a;" +
						             "          private int x;" +
						             "          int getX() { return this.x; } " +
						             "          B(A a0) { this.a = a0; } }"));
	}

	public void test22() {
		testFail(Program.fromClasses("class A { int x; class B { int y = x; } }"));
	}
}