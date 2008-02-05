package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.TypeDecl;
import AST.RefactoringException;

public abstract class RenameMethod extends TestCase {
	
	private static String TEST_BASE = "RenameMethod";

	public RenameMethod(String arg0) {
		super(arg0);
	}
	
	public void runMethodRenameTest(String name) {
        String infile = TEST_BASE+"/"+name+"/in/A.java";
        String resfile = TEST_BASE+"/"+name+"/out/A.java";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	Program prog = rename(fields[0], fields[1], fields[2], fields[3], fields[4]);
        	try {
        		File rf = new File(resfile);
        		FileReader rfr = new FileReader(rf);
        		long l = rf.length();
        		char[] buf = new char[(int)l];
        		rfr.read(buf);
        		assertEquals(new String(buf), prog.toString()+"\n");
        	} catch(FileNotFoundException fnfe) {
        		fail(name+" was supposed to fail but yielded result");
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file");
        } catch(RefactoringException rfe) {
        	assertFalse(new File(resfile).exists());
        }
	}

	private Program rename(String file, String pkg, String tp, String meth, String newname) 
			throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(file);
        assertNotNull(prog);
        String path[] = tp.split("\\.");
        TypeDecl d = (TypeDecl)prog.lookupType(pkg, path[0]);
        assertNotNull(d);
        for(int i=1;i<path.length;++i) {
        	iter = d.memberTypes(path[i]).iterator();
        	assertTrue(iter.hasNext());
            d = (TypeDecl)iter.next();
        }
        iter = d.localMethodsSignature(meth).iterator();
        assertTrue(iter.hasNext());
        MethodDecl m = (MethodDecl)iter.next();
        m.rename(newname);
        return prog;
	}

}
