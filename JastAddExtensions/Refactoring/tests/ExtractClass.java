package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.ClassDecl;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public abstract class ExtractClass extends TestCase {
	
	private static String TEST_BASE = "ExtractClass";

	public ExtractClass(String arg0) {
		super(arg0);
	}
	
	public void runExtractClassTest(String name) {
        String infile = TEST_BASE+File.separator+name+File.separator+"in"+File.separator+"A.java";
        String resfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A.java";
        String altfile = TEST_BASE+File.separator+name+File.separator+"out"+File.separator+"A_alt.java";
        try {
        	BufferedReader br = new BufferedReader(new FileReader(infile));
        	String cmd = br.readLine();
        	assertTrue(cmd.matches("^// .*$"));
        	String[] args = cmd.substring(3).split("\\s+");
        	String[] fields = new String[args.length-5];
    		System.arraycopy(args, 5, fields, 0, args.length-5);
    		Program prog = extractClass(args[0], args[1], args[2], fields, args[3], args[4]);
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
	
	private Program extractClass(String file, String pkg, String tp, String[] flds, String newClass, String newField) throws RefactoringException {
		Iterator iter;
		Program prog = TestHelper.compile(file);
        assertNotNull(prog);
		String path[] = tp.split("\\.");
		TypeDecl d = (TypeDecl) prog.lookupType(pkg, path[0]);
		assertNotNull(d);
		for (int i=1; i<path.length; ++i) {
			iter = d.memberTypes(path[i]).iterator();
        	assertTrue(iter.hasNext());
			d = (TypeDecl)iter.next();
		}
		ClassDecl cd = (ClassDecl) d;
		FieldDeclaration[] fieldDecls = new FieldDeclaration[flds.length];
		for(int i=0; i<flds.length; i++) {
			iter = cd.localFields(flds[i]).iterator();
			assertTrue(iter.hasNext());
			fieldDecls[i] = (FieldDeclaration) iter.next();
		}
		cd.extractClass(fieldDecls, newClass, newField);
		return prog;
	}

}
