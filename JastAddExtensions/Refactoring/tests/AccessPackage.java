package tests;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Access;
import AST.FileRange;
import AST.Program;

public abstract class AccessPackage extends TestCase {

	public AccessPackage(String arg0) {
		super(arg0);
	}
	
	public void runPackageAccessTest(String pkgname, FileRange rng, String expected) {
		Program prog = TestHelper.compile(rng.filename);
		assertNotNull(prog);
        ASTNode n = TestHelper.findSmallestCoveringNode(prog, rng);
        assertNotNull(n);
        Access res = n.accessPackage(pkgname);
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(res.toString(), expected);
        }
	}

}
