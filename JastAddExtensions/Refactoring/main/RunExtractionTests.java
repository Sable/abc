package main;

/*
 * Runs test cases for the Extract Method refactoring.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import AST.Block;
import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.MethodDecl;
import AST.Program;
import AST.Stmt;
import AST.TypeDecl;
import changes.RefactoringException;

public class RunExtractionTests extends Frontend {
	
    private static String TEST_BASE = "ExtractMethod";

    public static void main(String args[]) throws Throwable {
    	if(args.length > 0 && args[0].equals("show"))
    		SHOW = true;
    	int start = 0;
    	if(args.length > 1)
    		try {
    			start = Integer.parseInt(args[1]);
    		} catch(NumberFormatException nfe) { }
    	runTests(start);
	}
    
    private static void runTests(int start) throws Throwable {
        for(int i=start;;++i) {
            try {
                new RunExtractionTests().test("test"+i);
                if(!SHOW) System.out.println("test "+i+" passed");
            } catch(TestingException e) {
                if(!SHOW) System.out.println("test "+i+" failed: "+e);
            } catch(FileNotFoundException fnfe) {
            	return;
            }
            if(SHOW)
            	break;
        }
    }
    
    private static boolean SHOW = false;
    
    //private String oldprog;
    
    private void test(String name) throws Throwable {
        String file = TEST_BASE+"/"+name+"/in/A.java";
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String cmd = br.readLine();
        if(!cmd.matches("^// .*$"))
            throw new Exception("input file doesn't have the right format");
        String[] fields = cmd.substring(3).split("\\s+");
        Program prog = run(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), fields[5],
        				   fields.length > 6 ? fields[6] : "private");
        if(SHOW && prog != null) {
        	System.out.println(prog);
        	/*prog.undo();
        	if(!prog.toString().equals(oldprog))
        		System.out.println("before:\n"+oldprog+"\n\nnow:\n"+prog);*/
        } else {
        	String res = TEST_BASE+"/"+name+"/out/A.java";
        	try {
        		File rf = new File(res);
        		FileReader rfr = new FileReader(rf);
        		long l = rf.length();
        		char[] buf = new char[(int)l];
        		rfr.read(buf);
        		if(prog == null)
        			throw new TestingException("test "+name+" failed unexpectedly");
        		if(!new String(buf).equals(prog.toString()+"\n"))
        			throw new TestingException("test "+name+" gave wrong result:\n"+prog.toString()+"\n instead of "+new String(buf));
        	} catch(FileNotFoundException fnfe) {
        		if(prog != null)
        			throw new TestingException("test "+name+" was supposed to fail but yielded\n"+program.toString());
        	}
        }
    }

    private Program run(String file, String tp, String meth, int start, int end, String name, String vis) throws Throwable {
        String[] filenames = { file };
        if(!process(filenames, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        }
        ))
            throw new Exception("couldn't process input file");
     //oldprog = program.toString();
		String path[] = tp.split("\\.");
		TypeDecl d = (TypeDecl)program.lookupType("", path[0]);
		for(int i=1;i<path.length;++i) {
			d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
		}
		MethodDecl md = (MethodDecl)d.memberMethods(meth).iterator().next();
		Stmt start_stmt = md.getBlock().getStmt(start);
		Stmt end_stmt = md.getBlock().getStmt(end);
		try {
			d.compilationUnit().extractBlock(start_stmt, end_stmt);
			int i;
			for(i=start;i<md.getBlock().getNumStmt();++i)
				if(md.getBlock().getStmt(i) instanceof Block)
					break;
			Block blk = (Block)md.getBlock().getStmt(i);
			d.compilationUnit().makeMethod(name, vis, blk);
		} catch(RefactoringException rfe) {
			if(SHOW)
				System.out.println("refactoring failed: "+rfe);
			return null;
		}
		return program;
    }
    
}
