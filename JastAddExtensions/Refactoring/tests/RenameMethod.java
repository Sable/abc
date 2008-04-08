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
	
	public RenameMethod(String arg0) {
		super(arg0);
	}
	
	protected abstract String getTestBase();
	
	public void runMethodRenameTest(String name) {
        String infile = getTestBase()+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = getTestBase()+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String alt_resfile = getTestBase()+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	String[] files = fields[0].split(",");
        	Program prog = rename(files, fields[1], fields[2], fields[3], fields[4]);
        	try {
        		String res = new String(TestHelper.wholeFile(resfile));
        		if(!res.equals(prog.toString()+"\n")) {
        			res = new String(TestHelper.wholeFile(alt_resfile));
            		assertEquals(res, prog.toString()+"\n");
        		}
        	} catch(FileNotFoundException fnfe) {
        		fail(name+" was supposed to fail but yielded result");
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file");
        } catch(RefactoringException rfe) {
        	assertFalse(new File(resfile).exists());
        }
	}

	private Program rename(String[] files, String pkg, String tp, String meth, String newname) 
			throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(files);
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
