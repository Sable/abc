package tests;

import junit.framework.TestCase;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;

public class ExtractConstantTests extends TestCase {
	public ExtractConstantTests(String name) {
		super(name);
	}
	
	public void testSucc(Program in, Program out) {		
		assertNotNull(in);
		String originalProgram = in.toString();
		Program.startRecordingASTChanges();
		assertNotNull(out);
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtractConstant("C");
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
		Program.startRecordingASTChanges();
		Expr e = in.findDoublyParenthesised();
		assertNotNull(e);
		e.unparenthesise();
		try {
			e.doExtractConstant("C");
			assertEquals("<failure>", in.toString());
		} catch(RefactoringException rfe) { }
		in.undoAll();
		assertEquals(originalProgram, in.toString());
	}

    public void test1() {
        testSucc(
        	Program.fromBodyDecls(
        	"int m() {" +
        	"  return ((23));" +
        	"}"),
        	Program.fromBodyDecls(
        	"int m() {" +
        	"  return C;" +
        	"}",
        	"private static final int C = 23;"));
    }
    
    public void test2() {
        testSucc(
           	Program.fromBodyDecls(
           	"int m() {" +
           	"  return ((f()));" +
           	"}",
           	"static int f() {" +
           	"  return 23;" +
           	"}"),
           	Program.fromBodyDecls(
           	"int m() {" +
           	"  return C;" +
           	"}",
           	"static int f() {" +
           	"  return 23;" +
           	"}",
           	"private static final int C = f();"));    	
    }

    public void test3() {
        testFail(
           	Program.fromBodyDecls(
           	"int m() {" +
           	"  return ((f()));" +
           	"}",
           	"int f() {" +
           	"  return 23;" +
           	"}"));
    }

    public void test4() {
        testFail(
           	Program.fromBodyDecls(
           	"Object m() {" +
           	"  return ((null));" +
           	"}"));
    }
}