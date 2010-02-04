package tests;

import junit.framework.TestCase;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;

public class ExtractTempTests extends TestCase {
	public ExtractTempTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		assertNotNull(out);
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtract("x");
			assertEquals(out.toString(), in.toString());
		} catch(RefactoringException rfe) {
			fail("Refactoring was supposed to succeed; failed with "+rfe);
		}
	}

	public void testFail(Program in) {		
		assertNotNull(in);
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtract("x");
			fail("Refactoring was supposed to fail; succeeded with "+in);
		} catch(RefactoringException rfe) { }
	}

    public void test1() {
        testSucc(Program.fromStmts("System.out.println(((42)));"),
        		 Program.fromStmts("int x = 42;", "System.out.println(x);"));
    }

    public void test2() {
        testFail(Program.fromStmts("int x;", "System.out.println(((42)));"));
    }

    public void test3() {
        testFail(
        	Program.fromBodyDecls(
        	"void m() {" +
        	"  System.out.println(n()+\" \"+((n())));" +
        	"}",
        	"int x = 42;",
        	"int n() {" +
        	"  return ++x;" +
        	"}"));
    }

    public void test4() {
    	testFail(Program.fromBodyDecls(
    		"int y = 42;",
    		"void m() { System.out.println(y++ + \" \" + ((y))); }"));
    }

    public void test5() {
    	testFail(Program.fromStmts(
    		"int y = 42;",
    		"System.out.println(y++ + \" \" + ((y)));"));
    }

    public void test6() {
    	testFail(Program.fromStmts(
    		"int y = 23;",
    		"System.out.println((y=42) + \" \" + ((y=56)));",
    		"System.out.println(y);"));
    }
    
    public void test7() {
    	testFail(Program.fromBodyDecls(
    		"int f;",
    		"volatile boolean ready;",
    		"void m() {" +
    		"  p(f++, ((ready=true)));" +
    		"}",
    		"private void p(int i, boolean b) { }"));
    }
}
