package tests;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Access;
import AST.FileRange;
import AST.Program;
import AST.TypeDecl;

public abstract class AccessType extends TestCase {

	public AccessType(String arg0) {
		super(arg0);
	}
	
	public void runTypeAccessTest(FileRange typeloc, FileRange obsloc, Access expected) {
		Program prog;
		if(typeloc.filename.equals(obsloc.filename)) {
			prog = TestHelper.compile(typeloc.filename);
		} else {
			prog = TestHelper.compile(typeloc.filename, obsloc.filename);
		}
		assertNotNull(prog);
        ASTNode m = TestHelper.findSmallestCoveringNode(prog, typeloc);
        assertNotNull(m);
        assertTrue(m instanceof TypeDecl);
        ASTNode n = TestHelper.findSmallestCoveringNode(prog, obsloc);
        assertNotNull(n);
        assertTrue(n instanceof Access);
        Access res = ((Access)n).accessType((TypeDecl)m);
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(res.dumpTree(), expected.dumpTree());
        }
	}

}
