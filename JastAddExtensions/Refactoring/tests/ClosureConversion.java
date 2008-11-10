package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import AST.Application;
import AST.Block;
import AST.Closure;
import AST.ExprStmt;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class ClosureConversion extends TestCase {
	private static String TEST_BASE = "ClosureConversion";

	public ClosureConversion(String name) {
		super(name);
	}
	
	public void runTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        Program p = TestHelper.compile(infile);
        assertNotNull(p);
        Stmt stmt = TestHelper.findCommentedStmt(p, "// here\n");
        assertTrue(stmt instanceof Block);
        try {
            Closure closure = ((Block)stmt).wrapIntoClosure();
			closure.convert();
			String expected = new String(TestHelper.wholeFile(resfile));
			expected = expected.substring(0, expected.length()-1);
			String actual = p.toString();
			assertEquals(expected, actual);
		} catch (RefactoringException e) {
			assertFalse(name+" failed unexpectedly", new File(resfile).exists());
		} catch (FileNotFoundException e) {
			fail(name+" was supposed to fail but yielded result");
		} catch (IOException e) {
			fail("unable to read from result file: "+e);
		}
	}
}
