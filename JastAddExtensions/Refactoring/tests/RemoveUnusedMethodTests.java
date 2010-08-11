package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.MethodDecl;
import AST.Program;
import AST.RawCU;
import AST.RefactoringException;
import AST.TypeDecl;

public class RemoveUnusedMethodTests extends TestCase {
	public RemoveUnusedMethodTests(String name) {
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
			md.removeUnused(false);
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
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
			md.removeUnused(false);
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

    public void test1() {
    	testSucc(Program.fromClasses("class A { void m() { } }"),
    			 Program.fromClasses("class A { }"));
    }

    public void test2() {
    	testSucc(Program.fromClasses("class A { private void m() { } }"),
    			 Program.fromClasses("class A { }"));
    }

    public void test3() {
    	testSucc(Program.fromClasses("class A { void m(boolean b) { if(b) m(false); } }"),
    			 Program.fromClasses("class A { }"));
    }

    public void test4() {
    	testSucc(Program.fromClasses("class A { void m() { } void n() { m(); } }"),
    			 Program.fromClasses("abstract class A { abstract void m(); void n() { m(); } }"));
    }

    public void test5() {
    	testFail(Program.fromClasses("class A { void m() { } void n() { m(); } { new A(); } }"));
    }

    public void test6() {
    	testFail(Program.fromClasses("interface I { void m(); }",
    			                     "class A implements I { public void m() { } }",
    			                     "class B extends A { }",
    			                     "class C { { I i = new B(); i.m(); } }"));
    }

    public void test7() {
    	testSucc(Program.fromClasses("class A { void m() { } }",
    			                     "class B extends A { void n() { m(); } }"),
    			 Program.fromClasses("abstract class A { abstract void m(); }",
    			                     "abstract class B extends A { void n() { m(); } }"));
    }

    public void test8() {
    	testFail(Program.fromClasses("class A { void m() { } }",
    			                     "class B extends A { void n() { m(); } }",
    			                     "class C { B b = new B(); } "));
    }

    public void test9() {
    	testFail(Program.fromClasses("class A { void m() { } void n() { m(); } }",
    			                     "class B extends A { void m() { super.m(); } }"));
    }
}
