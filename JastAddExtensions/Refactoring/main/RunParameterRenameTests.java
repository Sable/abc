package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.ConstructorDecl;
import AST.Frontend;
import AST.JavaParser;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.Program;
import AST.TypeDecl;
import changes.ASTChange;
import changes.RefactoringException;

public class RunParameterRenameTests extends Frontend {

    public static void main(String[] args) throws Throwable {
        RunParameterRenameTests r = new RunParameterRenameTests();
        //r.test(args[0]);
        String[] files = {args[0]};
        Program prog = r.test(files, args[1], args[2], args[3], args[4], args[5]);
        if(prog == null)
            throw new Exception("failed to process input file");
        System.out.println(prog);
    }
    
    private void test(String name) throws Throwable {
        String file = name;
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String cmd = br.readLine();
        if(!cmd.matches("^// .*$"))
            throw new Exception("input file doesn't have the right format");
        String[] fields = cmd.substring(3).split("\\s+");
        String[] files = fields[0].split(",");
        Program prog = test(files, fields[1], fields[2], fields[3], fields[4], fields[5]);
        if(prog == null)
            throw new Exception("failed to process input file");
        System.out.println(prog);
    }

    private Program test(String[] files, String pkg, String tp, String meth, String parm, String n) throws RefactoringException {
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
            java.util.List changes;
            if(meth.equals("")) {
                ConstructorDecl cd = (ConstructorDecl)d.constructors().iterator().next();
                ParameterDeclaration pd =
                    (ParameterDeclaration)cd.parameterDeclaration(parm).iterator().next();
                changes = pd.rename(n);
            } else {
                MethodDecl md = (MethodDecl)d.memberMethods(meth).iterator().next();
                ParameterDeclaration pd = 
                    (ParameterDeclaration)md.parameterDeclaration(parm).iterator().next();
                changes = pd.rename(n);
            }
            for(Iterator i=changes.iterator();i.hasNext();) {
                ASTChange ch = (ASTChange)i.next();
                //System.out.println(ch.prettyprint());
                ch.apply();
            }
            return program;
        } else {
            throw new RefactoringException("couldn't compile input file");
        }
    }

}
