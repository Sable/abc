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
        assertTrue(m instanceof FieldDeclaration);
        ASTNode n = TestHelper.findSmallestCoveringNode(prog, obsloc);
        assertNotNull(n);
        Access res = null;
        if(n instanceof FieldDeclaration)
        	res = ((FieldDeclaration)n).accessField((FieldDeclaration)m);
        else if(n instanceof Block)
        	res = ((Block)n).accessField((FieldDeclaration)m);
        else if(n instanceof List)
        	res = ((TypeDecl)n.getParent()).accessField((FieldDeclaration)m);
        else if(n instanceof Expr)
        	res = ((Expr)n).accessField((FieldDeclaration)m);
        else
        	fail("not a valid location");
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(res.dumpTree(), expected.dumpTree());
        }
	}

}
