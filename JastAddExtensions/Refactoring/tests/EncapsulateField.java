package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public abstract class EncapsulateField extends TestCase {
	
	private static String TEST_BASE = "Encapsulate";

	public EncapsulateField(String arg0) {
		super(arg0);
	}
	
	public void runEncapsulationTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String altfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] fields = cmd.substring(3).split("\\s+");
        	Program prog = encapsulate(fields[0], fields[1], fields[2], fields[3]);
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
        		fail(name+" was supposed to fail but yielded result "+prog);
        	}
        } catch(IOException ioe) {
        	fail("unable to read from file "+ioe);
        } catch(RefactoringException rfe) {
        	assertFalse(new File(resfile).exists());
        }
	}

	private Program encapsulate(String file, String pkg, String tp, String fld) 
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
        iter = d.localFields(fld).iterator();
        assertTrue(iter.hasNext());
        FieldDeclaration f = (FieldDeclaration)iter.next();
        f.encapsulate();
        return prog;
	}

}
