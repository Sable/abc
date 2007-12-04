package main;

import java.util.Iterator;
import java.util.List;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.TypeDecl;
import changes.ASTChange;
import changes.RefactoringException;

public class EncapsulateTest extends Frontend {

	public static void main(String args[]) throws Throwable {
        EncapsulateTest e = new EncapsulateTest();
        e.encapsulate(args[0], args[1], args[2], args[3]);
	}
    
    private void encapsulate(String file, String pkg, String tp, String fld) throws Throwable {
        String[] filenames = { file };
        if(!process(filenames, new BytecodeParser(), 
                new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
        }
        ))
            throw new Exception("couldn't process input file");
		String path[] = tp.split("\\.");
		TypeDecl d = (TypeDecl)program.lookupType(pkg, path[0]);
		for(int i=1;i<path.length;++i) {
			d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
		}
		FieldDeclaration f = (FieldDeclaration)d.localFields(fld).iterator().next();
        try {
            List changes = f.encapsulate();
            for(Iterator i=changes.iterator();i.hasNext();) {
                ASTChange ch = (ASTChange)i.next();
                ch.apply();
                //System.out.println(ch.prettyprint());
            }
            System.out.println(program);
        } catch(RefactoringException e) {
            System.out.println("couldn't refactor: "+e);
        }
	}
    
}
