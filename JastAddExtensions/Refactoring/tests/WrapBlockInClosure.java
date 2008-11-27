package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;
import AST.Block;
import AST.CompilationUnit;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class WrapBlockInClosure extends TestCase {
	private static String TEST_BASE = "WrapBlockInClosure";

	public WrapBlockInClosure(String name) {
		super(name);
	}
	
	public void runWrappingTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        Program p = TestHelper.compile(infile);
        assertNotNull(p);
        CompilationUnit cu = p.lookupType("", "A").compilationUnit();
        assertNotNull(cu);
        Stmt stmt = TestHelper.findStmtFollowingComment(cu, "// here\n");
        assertTrue(stmt instanceof Block);
        try {
			((Block)stmt).wrapIntoClosure();
			String expected = new String(TestHelper.wholeFile(resfile));
			expected = expected.substring(0, expected.length()-1);
			String actual = p.toString();
			assertEquals(expected, actual);
		} catch (RefactoringException e) {
			e.printStackTrace();
			assertFalse(name+" failed unexpectedly", new File(resfile).exists());
		} catch (FileNotFoundException e) {
			fail(name+" was supposed to fail but yielded result "+p);
		} catch (IOException e) {
			fail("unable to read from result file: "+e);
		}
	}
}
