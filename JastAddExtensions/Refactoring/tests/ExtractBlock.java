package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Block;
import AST.ConstCase;
import AST.IntegerLiteral;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;

public class ExtractBlock extends TestCase {
	private static String TEST_BASE = "ExtractBlock";

	public ExtractBlock(String name) {
		super(name);
	}
	
	public void runTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        Program p = TestHelper.compile(infile);
        assertNotNull(p);
        Stmt from = TestHelper.findCommentedStmt(p, "// from\n");
        assertNotNull(from);
        Stmt to = TestHelper.findStmtFollowedByComment(p, "// to\n");
        assertNotNull(to);
        Block block = from.hostBlock();
        assertEquals(block, to.hostBlock());
        int start = block.getStmtList().getIndexOfChild(from);
        int end = block.getStmtList().getIndexOfChild(to);
        assertNotSame(start, -1);
        assertNotSame(end, -1);
        try {
			block.extractBlock(start, end);
			String expected = new String(TestHelper.wholeFile(resfile));
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