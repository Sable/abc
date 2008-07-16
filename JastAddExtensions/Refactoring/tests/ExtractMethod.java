package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.Block;
import AST.ConstructorDecl;
import AST.Methodoid;
import AST.Program;
import AST.RefactoringException;
import AST.Stmt;
import AST.TypeDecl;

public abstract class ExtractMethod extends TestCase {
	
	private static String TEST_BASE = "ExtractMethod";

	public ExtractMethod(String arg0) {
		super(arg0);
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
        		fail(name+" was supposed to fail but yielded result");
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file "+ioe);
        } catch(RefactoringException rfe) {
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
		Methodoid md = findMethodoid(d, meth);
		Stmt start_stmt = md.getBlock().getStmt(start);
		Stmt end_stmt = md.getBlock().getStmt(end);
		d.compilationUnit().extractBlock(start_stmt, end_stmt);
		int i;
		for(i=start;i<md.getBlock().getNumStmt();++i)
			if(md.getBlock().getStmt(i) instanceof Block)
				break;
		assertTrue(i != md.getBlock().getNumStmt());
		Block blk = (Block)md.getBlock().getStmt(i);
		d.compilationUnit().makeMethod(name, vis, blk);
		return prog;
	}
	
	private Methodoid findMethodoid(TypeDecl td, String name) {
		for(ConstructorDecl c : (Collection<ConstructorDecl>)td.constructors()) {
			if(c.name().equals(name))
				return c;
		}
		Iterator iter = td.memberMethods(name).iterator();
		assertTrue(iter.hasNext());
		return (Methodoid)iter.next();
	}

}
