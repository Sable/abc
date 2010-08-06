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
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChangesAndFlush();
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
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChangesAndFlush();
		TypeDecl td = in.findType("A");
		assertNotNull(td);
		MethodDecl md = td.findMethod("m");
		assertNotNull(md);
		try {
			md.doPullUp();
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		in.undoAll();
		assertEquals(originalProgram, in.toString());
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
}
