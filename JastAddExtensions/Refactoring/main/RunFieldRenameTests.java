package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import AST.ASTNode;
import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.TypeDecl;
import changes.ASTChange;
import changes.RefactoringException;

public class RunFieldRenameTests extends Frontend {

    private static String TEST_BASE = "RenameField";

    public static void main(String[] args) throws Throwable {
        if(args.length == 0)
            runTests();
        else
            showcase(args[0]);
    }

    private static void runTests() {
        try {
            for(int i=1;i<=17;++i) {
                if(i == 14 || i == 15) continue;
                try {
                    RunFieldRenameTests t = new RunFieldRenameTests();
                    t.test("test"+i);
                    t.check_results("test"+i);
                    System.out.println("test "+i+" passed");
                } catch(RefactoringException e) {
                    System.out.println("test "+i+" failed!");
                }
            }
        } catch(Throwable t) {
            System.err.println("Unexpected exception: "+t);
        }
    }
    
    private static void showcase(String i) {
        try {
            RunFieldRenameTests t = new RunFieldRenameTests();
            t.test("test"+i);
            System.out.println(t.program);
        } catch(Throwable e) {
            System.err.println("Exception: "+e);
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
        ASTNode prog = test(fields[0], fields[1], fields[2], fields[3], fields[4]);
        if(prog == null)
            throw new Exception("failed to process input file");
    }
    
    private void check_results(String name) throws Throwable {
        String res = TEST_BASE+"/"+name+"/out/A.java";
        try {
            File rf = new File(res);
            FileReader rfr = new FileReader(rf);
            long l = rf.length();
            char[] buf = new char[(int)l];
            rfr.read(buf);
            if(program == null)
                throw new RefactoringException("test "+name+" failed unexpectedly");
            if(!new String(buf).equals(program.toString()+"\n"))
                throw new RefactoringException("test "+name+" gave wrong result: "+program.toString()+"\n instead of "+new String(buf));
        } catch(FileNotFoundException fnfe) {
            if(program != null)
                throw new RefactoringException("test "+name+" was supposed to fail but yielded "+program.toString());
        }
    }

    private ASTNode test(String file, String pkg, String tp, String fld, String n) throws RefactoringException {
        //System.out.println("processing file "+file);
        String[] files = {file};
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
            FieldDeclaration f = (FieldDeclaration)d.localFields(fld).iterator().next();
            f.rename(n);
            return program;
        } else {
            return null;
        }
    }

}
