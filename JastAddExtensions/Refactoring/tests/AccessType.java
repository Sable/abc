package tests;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Access;
import AST.FileRange;
import AST.ImportDecl;
import AST.NameType;
import AST.Program;
import AST.TypeDecl;
import AST.UnambiguousContext;

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
        Access res;
        if(n instanceof ImportDecl) {
        	res = ((ImportDecl)n).accessType((TypeDecl)m).getAccess(new UnambiguousContext());
        } else {
        	assertTrue(n instanceof Access);
        	Access acc = (Access)n;
            boolean ambiguous = acc.nameType() == NameType.AMBIGUOUS_NAME || 
            					acc.nameType() == NameType.EXPRESSION_NAME;
        	res = acc.accessType((TypeDecl)m, ambiguous);
        }
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(expected.dumpTree(), res.dumpTree());
        }
	}
	
	public void runTypeAccessTest(String pkg, String tp, FileRange obsloc, Access expected) {
		Program prog = TestHelper.compile(obsloc.filename);
		assertNotNull(prog);
		TypeDecl td = prog.lookupType(pkg, tp);
		assertNotNull(prog);
        ASTNode n = TestHelper.findSmallestCoveringNode(prog, obsloc);
        assertNotNull(n);
        Access res;
        if(n instanceof ImportDecl) {
        	res = ((ImportDecl)n).accessType(td).getAccess(new UnambiguousContext());
        } else {
        	assertTrue(n instanceof Access);
        	Access acc = (Access)n;
            boolean ambiguous = acc.nameType() == NameType.AMBIGUOUS_NAME || 
            					acc.nameType() == NameType.EXPRESSION_NAME;
        	res = acc.accessType(td, ambiguous);
        }
        if(expected == null) {
        	assertNull(res);
        } else {
        	assertNotNull(res);
        	assertEquals(expected.dumpTree(), res.dumpTree());
        }
	}

}
