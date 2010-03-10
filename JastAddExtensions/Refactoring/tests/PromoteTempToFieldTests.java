package tests;

import junit.framework.TestCase;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;
import AST.Variable;
import AST.VariableDeclaration;

public class PromoteTempToFieldTests extends TestCase {
	public PromoteTempToFieldTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		assertNotNull(out);
		VariableDeclaration e = in.findLocalVariable("x");
		assertNotNull(e);
		try {
			e.doPromoteToField();
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		VariableDeclaration e = in.findLocalVariable("x");
		assertNotNull(e);
		try {
			e.doPromoteToField();
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
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
    		"int fac(int y) {" +
    		"  int x;" +
    		"  if(y == 0) {" +
    		"    x = 1;" +
    		"  } else {" +
    		"    x = y; " +
    		"    x = fac(y-1) * x;" +
    		"  }" +
    		"  return x;" +
    		"}"));
    }
    
    // the tests below are modelled after tests from the Eclipse test suite
    // we don't support a lot of features that are tested (initialisation in constructor/field,
    // renaming the promoted field), and our dataflow analysis is very paranoid about method
    // calls, so we only pass a handful of Eclipse's tests
    
    public void test3() {
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

    public void test4() {
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

    public void test5() {
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

    public void test6() {
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
    	    "    void fx() {" +
    	    "      x = 23;" +
    	    "    }" +
    	    "    int s() { return 3; }" +
    	    "    private int x;" +
    	    "  };" +
    	    "}"));
    }
    
    public void test7() {
    	testSucc(
       	    Program.fromBodyDecls(
       	    "void m() {" +
       	    "  double[] x[];" +
       	    "}"),
       	    Program.fromBodyDecls(
       		"private double[] x[];" +
       		"void m() { }"));    	
    }
    
    public void test8() {
    	testFail(Program.fromStmts("class Local{}", "Local x;"));
    }
    
    public void test9() {
    	testFail(Program.fromBodyDecls("Object x;", "void m() { int x; }"));
    }
    
    public void test10() {
    	testFail(Program.fromBodyDecls("<T> void k(T t) { T x = null; }"));
    }

    public void test11() {
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