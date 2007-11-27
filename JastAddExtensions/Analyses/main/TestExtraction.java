package main;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.MethodDecl;
import AST.Stmt;
import AST.TypeDecl;

public class TestExtraction extends Frontend {
	
	public static void main(String args[]) throws Throwable {
        TestExtraction te = new TestExtraction();
        te.run(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]), args[6]);
	}
    
    private void run(String file, String pkg, String tp, String meth, int start, int end, String name) throws Throwable {
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
		MethodDecl md = (MethodDecl)d.memberMethods(meth).iterator().next();
		Stmt start_stmt = md.getBlock().getStmt(start);
		Stmt end_stmt = md.getBlock().getStmt(end);
		d.compilationUnit().extract(name, start_stmt, end_stmt);
		//md.toString();
		System.out.println(program);
    }
    
}
