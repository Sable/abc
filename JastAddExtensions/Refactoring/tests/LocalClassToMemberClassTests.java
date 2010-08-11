package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.LocalClassDeclStmt;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class LocalClassToMemberClassTests extends TestCase {
	public LocalClassToMemberClassTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		LocalClassDeclStmt lcd = in.findLocalClass("A");
		assertNotNull(lcd);
		try {
			lcd.doPromoteToMemberClass();
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
		LocalClassDeclStmt lcd = in.findLocalClass("A");
		assertNotNull(lcd);
		try {
			lcd.doPromoteToMemberClass();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException e) {
		}
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}
	
	public void test1() {
		testSucc(Program.fromClasses("class B { void m() { class A { } } }"),
				 Program.fromClasses("class B { private class A { } void m() { } }"));
	}
	
	public void test2() {
		testSucc(Program.fromClasses("class B { void m() { class A { } A a; } }"),
				 Program.fromClasses("class B { private class A { } void m() { A a; } }"));
	}
	
	public void test3() {
		testSucc(Program.fromClasses("class B { void m(final int x) { class A { int n() { return x; } } A a; } }"),
				 Program.fromClasses("class B {" +
				 		             "	private class A {" +
				 		             "	  private final int x;" +
				 		             "    private A(int x) {" +
				 		             "      this.x = x;" +
				 		             "    }" +
				 		             "    int n() {" +
				 		             "      return x;" +
				 		             "    }" +
				 		             "  }" +
				 		             "  void m(final int x) {" +
				 		             "    A a;" +
				 		             "  }" +
				 		             "}"));
	}
	
	public void test4() {
		testSucc(Program.fromClasses("class B { void m(final int x) { class A { int n() { return x+x; } } A a; } }"),
				 Program.fromClasses("class B {" +
				 		             "	private class A {" +
				 		             "	  private final int x;" +
				 		             "    private A(int x) {" +
				 		             "      this.x = x;" +
				 		             "    }" +
				 		             "    int n() {" +
				 		             "      return x+x;" +
				 		             "    }" +
				 		             "  }" +
				 		             "  void m(final int x) {" +
				 		             "    A a;" +
				 		             "  }" +
				 		             "}"));
	}
	
	public void test5() {
		testSucc(Program.fromClasses("class B { void m(final int x) { final int y = 23; class A { int n() { return x+y; } } A a; } }"),
				 Program.fromClasses("class B {" +
				 		             "	private class A {" +
				 		             "	  private final int x;" +
				 		             "    private final int y;" +
				 		             "    private A(int y, int x) {" +
				 		             "      this.y = y;" +
				 		             "      this.x = x;" +
				 		             "    }" +
				 		             "    int n() {" +
				 		             "      return x+y;" +
				 		             "    }" +
				 		             "  }" +
				 		             "  void m(final int x) {" +
				 		             "    final int y = 23;" +
				 		             "    A a;" +
				 		             "  }" +
				 		             "}"));
	}
	
	public void test6() {
		testSucc(Program.fromClasses("class B { int x; void m() { class A { int n() { return x; } } A a; } }"),
				 Program.fromClasses("class B {" +
				 					 "  int x;" +
				 		             "	private class A {" +
				 		             "    private A() { super(); }" +
				 		             "    int n() {" +
				 		             "      return x;" +
				 		             "    }" +
				 		             "  }" +
				 		             "  void m() {" +
				 		             "    A a;" +
				 		             "  }" +
				 		             "}"));
	}
	
	public void test7() {
		testSucc(Program.fromClasses("class Outer{" +
									 "  void f() {" +
									 "    final int x = 23;" +
									 "    class B {" +
									 "      void m() {" +
									 "        class A {" +
									 "          int n() { return x; }" +
									 "        }" +
									 "      }" +
									 "    }" +
									 "  }" +
									 "}"),
				 Program.fromClasses("class Outer{" +
						 			 "  void f() {" +
						 			 "    final int x = 23;" +
						 			 "    class B {" +
						 			 "      class A {" +
						 			 "        int n() { return x; }" +
						 			 "      }" +
						 			 "      void m() {" +
						 			 "      }" +
						 			 "    }" +
						 			 "  }" +
						 			 "}"));
	}
	
	public void test8() {
		testFail(Program.fromClasses("class B { class A { } void m() { class A { } } }"));
	}
	
	public void test9() {
		testSucc(Program.fromClasses("class B  {" +
									 "  B(int i) { }" +
									 "  void f() {" +
									 "    final class A extends B {" +
									 "      A(int i) { super(i); }" +
									 "    }" +
									 "    new A(1);" +
									 "  }" +
									 "}"),
				 Program.fromClasses("class B {" +
				 					 "  final private class A extends B {" +
				 					 "	  private A(int i) { super(i); }" +
				 					 "  }" +
				 					 "  B(int i) { }" +
				 					 "  void f() {" +
				 					 "	  new A(1);" +
				 					 "  }" +
				 					 "}"));
  }
}