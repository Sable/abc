package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.MethodAccess;
import AST.Program;
import AST.RefactoringException;

public abstract class InlineMethod extends TestCase {
	
	private static String TEST_BASE = "InlineMethod";

	public InlineMethod(String name) {
		super(name);
	}
	
	public void runInlineTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String altfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        Program p = TestHelper.compile(infile);
        assertNotNull(p);
        CompilationUnit cu = p.lookupType("", "A").compilationUnit();
        assertNotNull(cu);
        ASTNode node = TestHelper.findNodeBetweenComments(cu, "/*[*/", "/*]*/");
        assertTrue(node instanceof MethodAccess);
        MethodAccess ma = (MethodAccess)node;
        try {
        	ma.inline();
			String expected = new String(TestHelper.wholeFile(resfile));
			String actual = p.toString();
			if(!expected.equals(actual)) {
				expected = new String(TestHelper.wholeFile(altfile));
				assertEquals(expected, actual);
			}
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
