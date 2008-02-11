package tests;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Access;
import AST.Block;
import AST.Expr;
import AST.FieldDeclaration;
import AST.FileRange;
import AST.List;
import AST.Program;
import AST.TypeDecl;
import AST.Variable;

public abstract class AccessField extends TestCase {

	public AccessField(String arg0) {
		super(arg0);
	}
	
	public void runFieldAccessTest(FileRange fieldloc, FileRange obsloc, Access expected) {
		Program prog;
		if(fieldloc.filename.equals(obsloc.filename)) {
			prog = TestHelper.compile(fieldloc.filename);
		} else {
			prog = TestHelper.compile(fieldloc.filename, obsloc.filename);
		}
		assertNotNull(prog);
        ASTNode m = TestHelper.findSmallestCoveringNode(prog, fieldloc);
        assertNotNull(m);
        assertTrue(m instanceof Variable);
        ASTNode n = TestHelper.findSmallestCoveringNode(prog, obsloc);
        assertNotNull(n);
        assertTrue(n instanceof Access);
        Access res = ((Access)n).getAccessTo((Variable)m);
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(expected.dumpTree(), res.dumpTree());
        }
	}

}
