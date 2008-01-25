package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import main.TestingException;

import junit.framework.TestCase;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Program;
import AST.TypeDecl;
import changes.RefactoringException;

public abstract class RenameType extends TestCase {
	
	private static String TEST_BASE = "RenameType";

	public RenameType(String arg0) {
		super(arg0);
	}
	
	public void runTypeRenameTest(String name) {
        String infile = TEST_BASE+"/"+name+"/in/A.java";
        String outdir = TEST_BASE+"/"+name+"/out";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	String[] files = fields[0].split(",");        	
        	Program prog = rename(files, fields[1], fields[2], fields[3]);
            for(int i=0;i<prog.getNumCompilationUnit();++i) {
                CompilationUnit cu = prog.getCompilationUnit(i);
                if(cu.fromSource())
                    check_cu(name, cu);
            }
        } catch(IOException ioe) {
        	fail("unable to read from file");
        } catch(RefactoringException rfe) {
        	assertFalse(new File(outdir).exists());
        }
	}

    private void check_cu(String testname, CompilationUnit cu) throws FileNotFoundException, IOException{
        String filename = TEST_BASE+"/"+testname+"/out/"+cu.getID()+".java";
        File rf = new File(filename);
        FileReader rfr = new FileReader(rf);
        long l = rf.length();
        char[] buf = new char[(int)l];
        rfr.read(buf);
        assertEquals(new String(buf), cu+"\n");
    }
    
	private Program rename(String[] file, String pkg, String tp, String newname) 
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
        d.rename(newname);
        return prog;
	}

}
