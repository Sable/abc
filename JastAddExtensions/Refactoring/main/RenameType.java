package main;

import java.util.Collection;
import java.util.Iterator;

import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameType {
	
	public static void main(String[] args) {
		if(args.length < 4) {
			usage();
			return;
		}
		String pkg = args[0];
		String tp = args[1];
		String newname = args[2];
		String[] files = new String[args.length-3];
		System.arraycopy(args, 3, files, 0, files.length);
		rename(pkg, tp, newname, files);
	}
	
	public static void usage() {
		System.out.println("Usage: RenameType <pkg> <type> <new-name> <files...>");
	}
	
	static void rename(String pkg, String tp, String newname, String[] files) {
		Frontend f = new Frontend() { 
			protected void processErrors(Collection errors, CompilationUnit unit) { 
				super.processErrors(errors, unit);
			}
			protected void processWarnings(Collection errors, CompilationUnit unit) { }
		};
		BytecodeReader br = new BytecodeParser();
		JavaParser jp = new JavaParser() {
            public CompilationUnit parse(java.io.InputStream is, String fileName) 
            		throws java.io.IOException, beaver.Parser.Exception {
                return new parser.JavaParser().parse(is, fileName);
            }
		};
		if(f.process(files, br, jp)) {
			Program prog = f.getProgram();
	        String path[] = tp.split("\\.");
	        TypeDecl d = (TypeDecl)prog.lookupType(pkg, path[0]);
	        assert(d != null);
	        for(int i=1;i<path.length;++i) {
	        	Iterator iter = d.memberTypes(path[i]).iterator();
	        	assert(iter.hasNext());
	            d = (TypeDecl)iter.next();
	        }
	        try {
	        	d.rename(newname);
	        } catch(RefactoringException rfe) {
	        	System.err.println("refactoring failed");
	        	rfe.printStackTrace();
	        }
		} else {
			System.err.println("There were compilation errors.");
		}
	}
	
}
