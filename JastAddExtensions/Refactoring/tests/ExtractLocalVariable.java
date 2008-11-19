package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.Expr;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class ExtractLocalVariable extends TestCase {
	private static String TEST_BASE = "ExtractLocalVariable";

	public ExtractLocalVariable(String name) {
		super(name);
	}
	
	public void runTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        Program p = TestHelper.compile(infile);
        assertNotNull(p);
        CompilationUnit cu = p.lookupType("", "A").compilationUnit();
        assertNotNull(cu);
        ASTNode node = TestHelper.findNodeBetweenComments(cu, "/*[*/", "/*]*/");
        assertTrue(node instanceof Expr);
        Expr expr = (Expr)node;
        try {
        	expr.extractLocalVariable("x");
			String expected = new String(TestHelper.wholeFile(resfile));
			String actual = p.toString();
			assertEquals(expected, actual);
		} catch (RefactoringException e) {
			assertFalse(name+" failed unexpectedly", new File(resfile).exists());
		} catch (FileNotFoundException e) {
			fail(name+" was supposed to fail but yielded result "+p);
		} catch (IOException e) {
			fail("unable to read from result file: "+e);
		}
	}
	
}
