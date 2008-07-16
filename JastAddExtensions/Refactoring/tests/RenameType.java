package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.CompilationUnit;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public abstract class RenameType extends TestCase {
	
	public RenameType(String arg0) {
		super(arg0);
	}
	
	public abstract String getTestBase();
	
	public void runTypeRenameTest(String name) {
        String infile = getTestBase()+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String outdir = getTestBase()+File.separator+name+File.separator+"out";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	String[] files = fields[0].split(",");
        	Program prog;
        	if(fields.length == 3) {
        		prog = rename(files, fields[1], fields[2]);
        	} else {
        		assertTrue(fields.length == 4);
        		prog = rename(files, fields[1], fields[2], fields[3]);
        	}
            for(int i=0;i<prog.getNumCompilationUnit();++i) {
                CompilationUnit cu = prog.getCompilationUnit(i);
                if(cu.fromSource())
                    check_cu(name, cu);
            }
        } catch(IOException ioe) {
        	fail("unable to read from file: "+ioe);
        } catch(RefactoringException rfe) {
        	//rfe.printStackTrace();
        	assertFalse(new File(outdir).exists());
        }
	}

    private void check_cu(String testname, CompilationUnit cu) throws FileNotFoundException, IOException{
        String filename = getTestBase()+File.separator+testname+File.separator+"out"+File.separator+cu.getID()+".java";
        File rf = new File(filename);
        FileReader rfr = new FileReader(rf);
        long l = rf.length();
        char[] buf = new char[(int)l];
        rfr.read(buf);
    	rfr.close();
        /*if(!new String(buf).equals(cu+"\n")) {
        	System.out.println("fixing compilation unit "+cu.getID()+
        						" of test "+testname);
        	FileWriter rfw = new FileWriter(rf);
        	rfw.write(cu+"\n");
        	rfw.close();
        }*/
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

	private Program rename(String[] file, String tp, String newname) throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(file);
		assertNotNull(prog);
		TypeDecl td = findType(prog, tp);
		assertNotNull(td);
		td.rename(newname);
		return prog;
	}
	
	private TypeDecl findType(ASTNode n, String name) {
		if(n == null) return null;
		if(n instanceof TypeDecl &&	((TypeDecl)n).name().equals(name))
			return (TypeDecl)n;
		for(int i=0;i<n.getNumChild();++i) {
			TypeDecl td = findType(n.getChild(i), name);
			if(td != null) return td;
		}
		return null;
	}
	
}
