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

public class RunEncapsulationTests extends Frontend {

    private static String TEST_BASE = "Encapsulate";

    public static void main(String[] args) throws Throwable {
        runTests();
    }

    private static void runTests() throws Throwable {
        //try {
            for(int i=1;i<=10;++i) {
                try {
                    new RunEncapsulationTests().test("test"+i);
                    System.out.println("test "+i+" passed");
                } catch(TestingException e) {
                    System.out.println("test "+i+" failed: "+e);
                }
            }
        /*} catch(Throwable t) {
            System.err.println("Unexpected exception: "+t);
        }*/
    }

    private void test(String name) throws Throwable {
        String file = TEST_BASE+"/"+name+"/in/A.java";
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String cmd = br.readLine();
        if(!cmd.matches("^// .*$"))
            throw new Exception("input file doesn't have the right format");
        String[] fields = cmd.substring(3).split("\\s+");
        ASTNode prog = test(fields[0], fields[1], fields[2], fields[3]);
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

    private ASTNode test(String file, String pkg, String tp, String fld) {
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
            try {
                f.encapsulate();
                return program;
            } catch(RefactoringException rfe) {
                return null;
            }
        } else {
            return null;
        }
    }

}
