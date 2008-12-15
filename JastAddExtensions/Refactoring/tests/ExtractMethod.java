package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.Block;
import AST.Callable;
import AST.CompilationUnit;
import AST.ConstructorDecl;
import AST.Expr;
import AST.FileRange;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;
import AST.TypeDecl;

public class ExtractMethod extends TestCase {
	
	private static String TEST_BASE = "ExtractMethod";

	public ExtractMethod(String arg0) {
		super(arg0);
	}
	
	public static void main(String[] args) throws RefactoringException {
		Program prog = TestHelper.compile(args);
		Stmt from = null, to = null;
		for(CompilationUnit cu : prog.getCompilationUnits()) {
			if(!cu.fromSource())
				continue;
    		from = TestHelper.findStmtFollowingComment(cu, "// from\n");
    		if(from == null) continue;
    		to = TestHelper.findStmtPrecedingComment(cu, "// to\n");
    		if(to == null) continue;
		}
		if(from == null || to == null)
			throw new Error("couldn't find region to extract");
   		Block blk = from.hostBlock();
   		int fromIndex = blk.getIndexOfStmt(from);
   		int toIndex = blk.getIndexOfStmt(to);
   		if(fromIndex == -1 || toIndex == -1)
   			throw new Error("couldn't find region to extract");
   		long start = System.currentTimeMillis();
   		blk.extractMethod("protected", "extracted", fromIndex, toIndex);
   		long end = System.currentTimeMillis();
   		System.out.println(prog);
   		System.out.println(end-start);
	}
	
	public void runExtractionTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String altfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	assertTrue(fields.length >= 6);
            Program prog = extract(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), fields[5],
 				   			       fields.length > 6 ? fields[6] : "private");
        	try {
        		char[] buf = TestHelper.wholeFile(resfile);
        		if(new File(altfile).exists() && !new String(buf).equals(prog+"\n")) {
        			String res = new String(TestHelper.wholeFile(altfile));
        			/*if(!res.equals(prog+"\n")) {
        				System.out.println("fixing test "+name);
        				FileWriter rfw = new FileWriter(new File(altfile));
        				rfw.write(prog+"\n");
        				rfw.close();
        			}*/
        			assertEquals(res, prog+"\n");
        		} else {
        			String res = new String(buf);
        			/*if(!res.equals(prog+"\n")) {
        				System.out.println("fixing test "+name);
        				FileWriter rfw = new FileWriter(new File(resfile));
        				rfw.write(prog+"\n");
        				rfw.close();
        			}*/
        			assertEquals(res, prog+"\n");
        		}
        	} catch(FileNotFoundException fnfe) {
        		fail(name+" was supposed to fail but yielded result: "+prog);
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file "+ioe);
        } catch(RefactoringException rfe) {
        	rfe.printStackTrace();
        	assertFalse(new File(resfile).exists());
        }
	}
	
	public void runExtractionTest2(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String altfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        try {
        	Program prog = TestHelper.compile(infile);
        	/*System.out.println(prog);
        	prog.flushCaches();*/
        	assertNotNull(prog);
        	TypeDecl A = prog.lookupType("", "A");
        	assertNotNull(A);
        	CompilationUnit cu = A.compilationUnit();
        	assertNotNull(cu);
            FileRange startPos = cu.findComment("/*[*/");
            FileRange endPos = cu.findComment("/*]*/");
    		FileRange rng = startPos == null || endPos == null ? null : new FileRange("", startPos.el, startPos.ec, endPos.sl, endPos.ec);
            ASTNode n = TestHelper.findFirstNodeInside(cu, rng);
        	if(n instanceof Expr) {
        		Expr e = (Expr)n;
        		e.extractMethod("protected", "extracted");
        	} else if(n instanceof Stmt) {
        		Stmt s = (Stmt)n;
        		s.extractMethod("protected", "extracted");
        	} else {
        		Stmt from = TestHelper.findStmtFollowingComment(cu, "// from\n");
        		assertNotNull(from);
        		Stmt to = TestHelper.findStmtPrecedingComment(cu, "// to\n");
        		assertNotNull(to);
        		Block blk = from.hostBlock();
        		int fromIndex = blk.getIndexOfStmt(from);
        		assertTrue(fromIndex != -1);
        		int toIndex = blk.getIndexOfStmt(to);
        		assertTrue(toIndex != -1);
        		blk.extractMethod("protected", "extracted", fromIndex, toIndex);
        	}
        	try {
        		char[] buf = TestHelper.wholeFile(resfile);
        		if(new File(altfile).exists() && !new String(buf).equals(prog+"\n")) {
        			String res = new String(TestHelper.wholeFile(altfile));
        			assertEquals(res, prog+"\n");
        		} else {
        			String res = new String(buf);
        			assertEquals(res, prog+"\n");
        		}
        	} catch(FileNotFoundException fnfe) {
        		fail(name+" was supposed to fail but yielded result: "+prog);
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file "+ioe);
        } catch(RefactoringException rfe) {
        	rfe.printStackTrace();
        	assertFalse(new File(resfile).exists());
        }
	}
	
	private Program extract(String file, String tp, String meth, int start, int end, String name, String vis) 
			throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(file);
        assertNotNull(prog);
        String path[] = tp.split("\\.");
		TypeDecl d = (TypeDecl)prog.lookupType("", path[0]);
        assertNotNull(d);
        for(int i=1;i<path.length;++i) {
        	iter = d.memberTypes(path[i]).iterator();
        	assertTrue(iter.hasNext());
            d = (TypeDecl)iter.next();
        }
		Callable md = findCallable(d, meth);
		md.getBlock().extractMethod(vis, name, start, end);
		return prog;
	}
	
	private Callable findCallable(TypeDecl td, String name) {
		for(ConstructorDecl c : (Collection<ConstructorDecl>)td.constructors()) {
			if(c.name().equals(name))
				return c;
		}
		Iterator iter = td.memberMethods(name).iterator();
		assertTrue(iter.hasNext());
		return (Callable)iter.next();
	}

}
