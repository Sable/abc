package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;
import AST.Variable;

public abstract class InlineVariable extends TestCase {
	
	private static String TEST_BASE = "InlineVariable";

	public InlineVariable(String arg0) {
		super(arg0);
	}
	
	public void runInlineTest(String name) {
		String indir_name = TEST_BASE+File.separator+name+File.separator+"in";
		String outdir_name = TEST_BASE+File.separator+name+File.separator+"out";
		File testdir = new File(indir_name);
		assertTrue(testdir.isDirectory());
		String[] files = testdir.list();
		for(int i=0;i<files.length;++i)
			files[i] = indir_name+File.separator+files[i];
		assertTrue(files.length > 0);
		Program prog = null;
        try {
        	prog = inline(files);
            for(int i=0;i<prog.getNumCompilationUnit();++i) {
                CompilationUnit cu = prog.getCompilationUnit(i);
                if(cu.fromSource())
                    check_cu(outdir_name, cu);
            }
        } catch(IOException ioe) {
        	fail("test "+name+" was supposed to fail but yielded output "+prog);
        } catch(RefactoringException rfe) {
        	assertFalse(new File(outdir_name).exists());
        }
	}
	
    private void check_cu(String outdir_name, CompilationUnit cu) 
    		throws FileNotFoundException, IOException{
        char[] buf = TestHelper.wholeFile(outdir_name+File.separator+cu.getID()+".java");
        /*if(!new String(buf).equals(cu+"\n")) {
        	System.out.println("fixing compilation unit "+cu.getID());
        	FileWriter rfw = new FileWriter(rf);
        	rfw.write(cu+"\n");
        	rfw.close();
        }*/
        assertEquals(new String(buf), cu+"\n");
    }

	private Program inline(String[] files) throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(files);
        assertNotNull(prog);
		Variable v = (Variable)TestHelper.findVariable(prog, "i");
		assertNotNull(v);
        v.inline();
        return prog;
	}

}
