package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.TypeDecl;
import changes.ASTChange;
import changes.RefactoringException;

public class RunTypeRenameTests extends Frontend {

    private static String TEST_BASE = "RenameType";

    public static void main(String[] args) throws Throwable {
        runTests();
    }
    
    private static void runTests() {
        try {
            for(int i=0;i<86;++i) {
                if((62 <= i && i <= 65) || (i == 70) || (i == 21) ||
                        (79 <= i && i <= 81)) {
                    System.out.println("test "+i+" skipped");
                    continue;
                }
                try {
                    RunTypeRenameTests t = new RunTypeRenameTests();
                    t.test("test"+i);
                    System.out.println("test "+i+" passed");
                } catch(TestingException e) {
                    System.out.println("test "+i+" failed!");
                }
            }
        } catch(Throwable t) {
            System.err.println("Unexpected exception: "+t);
        }
    }

    private void test(String name) throws Throwable {
        String file = TEST_BASE+"/"+name+"/in/A.java";
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String cmd = br.readLine();
        if(!cmd.matches("^// .*$"))
            throw new Exception("input file doesn't have the right format");
        String[] fields = cmd.substring(3).split("\\s+");
        String[] files = fields[0].split(","); 
        try {
            Program prog = test(files, fields[1], fields[2], fields[3]);
            if(prog == null)
                throw new Exception("failed to process input file");
            try {
                for(int i=0;i<prog.getNumCompilationUnit();++i) {
                    CompilationUnit cu = prog.getCompilationUnit(i);
                    if(cu.fromSource())
                        check_cu(name, cu);
                }
            } catch(FileNotFoundException fnfe) {
                throw new TestingException("test "+name+" was supposed to fail");
            }
        } catch(RefactoringException e) {
            try {
                new FileReader(TEST_BASE+"/"+name+"/out");
                throw new TestingException("test "+name+" was supposed to succeed");
            } catch(FileNotFoundException fnfe) {
                // OK
            }
        }
    }
    
    private void check_cu(String testname, CompilationUnit cu) throws Throwable {
        String filename = TEST_BASE+"/"+testname+"/out/"+cu.getID()+".java";
        //System.out.println("trying file "+filename);
        File rf = new File(filename);
        FileReader rfr = new FileReader(rf);
        long l = rf.length();
        char[] buf = new char[(int)l];
        rfr.read(buf);
        if(!new String(buf).equals(cu.toString()+"\n")) {
            System.out.println("compilation unit "+cu.getID()+" should be\n"+new String(buf)+", but is\n"+cu.toString());
            throw new TestingException("result wasn't what I expected");
        }
    }
    
    private void mktest(String name) throws Throwable {
        String file = TEST_BASE+"/"+name+"/in/A.java";
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String cmd = br.readLine();
        if(!cmd.matches("^// .*$"))
            throw new Exception("input file doesn't have the right format");
        String[] fields = cmd.substring(3).split("\\s+");
        String[] files = fields[0].split(",");
        Program prog = test(files, fields[1], fields[2], fields[3]);
        if(prog == null)
            throw new Exception("failed to process input file");
        System.out.println(prog);
    }

    private Program test(String[] files, String pkg, String tp, String n) throws RefactoringException {
        if(process(files, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        })) {
            String path[] = tp.split("\\.");
            TypeDecl d = (TypeDecl)program.lookupType(pkg, path[0]);
            for(int i=1;i<path.length;++i)
                d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
            d.rename(n);
            return program;
        } else {
            return null;
        }
    }

}
