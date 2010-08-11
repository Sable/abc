package tests;

import junit.framework.TestCase;
import tests.AllTests;
import AST.Program;
import AST.RefactoringException;
import AST.VariableDeclaration;

public class PromoteTempToFieldTests extends TestCase {
	public PromoteTempToFieldTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		if (AllTests.TEST_UNDO) Program.startRecordingASTChangesAndFlush();
		assertNotNull(out);
		VariableDeclaration e = in.findLocalVariable("x");
		assertNotNull(e);
		try {
			e.doPromoteToField();
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
		VariableDeclaration e = in.findLocalVariable("x");
		assertNotNull(e);
		try {
			e.doPromoteToField();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
		if (AllTests.TEST_UNDO) { Program.undoAll(); in.flushCaches(); }
		if (AllTests.TEST_UNDO) assertEquals(originalProgram, in.toString());
		Program.stopRecordingASTChangesAndFlush();
	}

    public void test1() {
    	testSucc(
    	    Program.fromBodyDecls(
    	    "int sum(int... ys) {" +
    	    "  int x = 0;" +
    	    "  for(int y : ys)" +
    	    "    x += y;" +
    	    "  return x;" +
    	    "}"),
    	    Program.fromBodyDecls(
    	    "private int x;" +
    	    "int sum(int... ys) {" +
    	    "  x = 0;" +
    	    "  for(int y : ys)" +
    	    "    x += y;" +
    	    "  return x;" +
    	    "}"));
    }
    
    public void test2() {
    	testFail(
    		Program.fromBodyDecls(
    		"int f(int y) {" +
    		"  if(y <= 1)" +
    		"    return 1;" +
    		"  int x = y;" +
    		"  return f(y-1) * x;" +
    		"}"));
    }
    
    public void test3() {
    	testSucc(
        		Program.fromBodyDecls(
        		"int f(int y) {" +
        		"  if(y <= 1)" +
        		"    return 1;" +
        		"  int x = y;" +
        		"  return x * f(y-1);" +
        		"}"),
        		Program.fromBodyDecls(
        		"private int x;" +
        		"int f(int y) {" +
        		"  if(y <= 1)" +
        		"    return 1;" +
        		"  x = y;" +
        		"  return x * f(y-1);" +
        		"}"));
    }
    
    public void test4() {
    	testSucc(
    		Program.fromClasses(
    		"class Super { int x = 42; }",
    		"class A extends Super {" +
    		"  int f() { return x; }" +
    		"  void m() {" +
    		"    int x = 23;" +
    		"  }" +
    		"}"),
    		Program.fromClasses(
    		"class Super { int x = 42; }",
    		"class A extends Super {" +
    		"  int f() { return super.x; }" +
    		"  private int x;" +
    		"  void m() {" +
    		"    x = 23;" +
    		"  }" +
    		"}"));
    }
    
    // the tests below are modelled after tests from the Eclipse test suite
    // we don't support a lot of features that are tested (initialisation in constructor/field,
    // renaming the promoted field), and our dataflow analysis is very paranoid about method
    // calls, so we only pass a handful of Eclipse's tests
    
    public void test5() {
    	testSucc(
        	Program.fromClasses(
        	"class A<T> {" +
        	"  void m(T arg) {" +
        	"    T x = arg;" +
        	"  }" +
        	"}"),
        	Program.fromClasses(
        	"class A<T> {" +
        	"  private T x;" +
        	"  void m(T arg) {" +
        	"    x = arg;" +
        	"  }" +
        	"}"));
    }

    public void test6() {
    	testSucc(
    	    Program.fromBodyDecls(
    	    "enum Member { FIRST, SECOND; }" +
    	    "void use() {" +
    	    "  Member x = Member.SECOND;" +
    	    "  Member y = x;" +
    	    "}"),
    	    Program.fromBodyDecls(
    	    "enum Member { FIRST, SECOND; }" +
    	    "private Member x;" +
    	    "void use() {" +
    	    "  x = Member.SECOND;" +
    	    "  Member y = x;" +
    	    "}"));
    }

    public void test7() {
    	testSucc(
    	    Program.fromBodyDecls(
    	    "void f() {" +
    	    "  int x = 0;" +
    	    "  x++;" +
    	    "}"),
    	    Program.fromBodyDecls(
    	    "private int x;" +
    	    "void f() {" +
    	    "  x = 0;" +
    	    "  x++;" +
    	    "}"));
    }

    public void test8() {
    	testSucc(
    	    Program.fromBodyDecls(
    	    "void f() {" +
    	    "  new Object() {" +
    	    "    void fx() {" +
    	    "      int x = 23;" +
    	    "    }" +
    	    "    int s() { return 3; }" +
    	    "  };" +
    	    "}"),
    	    Program.fromBodyDecls(
    		"void f() {" +
    	    "  new Object() {" +
    	    "    private int x;" +
    	    "    void fx() {" +
    	    "      x = 23;" +
    	    "    }" +
    	    "    int s() { return 3; }" +
    	    "  };" +
    	    "}"));
    }
    
    public void test9() {
    	testSucc(
       	    Program.fromBodyDecls(
       	    "void m() {" +
       	    "  double[] x[];" +
       	    "}"),
       	    Program.fromBodyDecls(
       		"private double[] x[];" +
       		"void m() { }"));    	
    }
    
    public void test10() {
    	testFail(Program.fromStmts("class Local{}", "Local x;"));
    }
    
    public void test11() {
    	testFail(Program.fromBodyDecls("Object x;", "void m() { int x; }"));
    }
    
    public void test12() {
    	testFail(Program.fromBodyDecls("<T> void k(T t) { T x = null; }"));
    }

    public void test13() {
    	testSucc(
       	    Program.fromBodyDecls(
       	    "static int m() {" +
       	    "  int x = 23;" +
       	    "  return x;" +
       	    "}"),
       	    Program.fromBodyDecls(
       		"private static int x;" +
       		"static int m() {" +
       		"  x = 23;" +
       		"  return x;" +
       		"}"));    	
    }
    
}