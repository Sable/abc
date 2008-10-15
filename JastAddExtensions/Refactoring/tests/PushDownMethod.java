package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public abstract class PushDownMethod extends TestCase {
	
	public PushDownMethod(String arg0) {
		super(arg0);
	}
	
	protected abstract String getTestBase();
	
	public void runPushDownMethodTest(String name) {
        String infile = getTestBase()+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = getTestBase()+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String alt_resfile = getTestBase()+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        Program prog = null;
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	String[] files = fields[0].split(",");
        	prog = pushdown(files, fields[1], fields[2], fields[3]);
        	try {
        		String res = new String(TestHelper.wholeFile(resfile));
        		if(!res.equals(prog.toString()+"\n")) {
        			if(new File(alt_resfile).exists()) {
        				res = new String(TestHelper.wholeFile(alt_resfile));
        				assertEquals(res, prog.toString()+"\n");
        			} else {
        				assertEquals(res, prog.toString()+"\n");
        			}
        		}
        	} catch(FileNotFoundException fnfe) {
        		fail(name+" was supposed to fail but yielded result "+prog);
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file");
        } catch(RefactoringException rfe) {
        	assertFalse(new File(resfile).exists());
        }
	}

	private Program pushdown(String[] files, String pkg, String tp, String meth) 
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
        m.pushDown();
        return prog;
	}

}
