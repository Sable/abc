package main;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import AST.BytecodeParser;
import AST.BytecodeReader;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameType {
	
	public static final int NUM_RUNS = 5;
	
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
		long time = System.currentTimeMillis();
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
			System.out.println("time to load: "+(System.currentTimeMillis()-time)+"\n");
			for(int cnt=0;cnt<NUM_RUNS;cnt++) {
				time = System.currentTimeMillis();
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
					System.out.println("total: "+(System.currentTimeMillis()-time));
					if(cnt == 0 && hasErrors(prog))
						System.err.println("Output program has errors!");
					time = System.currentTimeMillis();
					prog.undo();
					System.out.println("undo: "+(System.currentTimeMillis()-time)+"\n");
					prog.flushCaches();
				} catch(RefactoringException rfe) {
					System.err.println("refactoring failed");
					rfe.printStackTrace();
				}
			}
		} else {
			System.err.println("There were compilation errors.");
		}
	}
	
	protected static boolean hasErrors(Program p) {
		boolean has_errors = false;
		Collection errors, warnings;
		for(int i=0;i<p.getNumCompilationUnit();++i) {
			AST.CompilationUnit cu = p.getCompilationUnit(i);
			if(!cu.fromSource())
				continue;
			errors = cu.parseErrors();
			warnings = new LinkedList();
			cu.errorCheck(errors, warnings);
			if(!errors.isEmpty()) {
				has_errors = true;
				break;
			}
		}
		return has_errors;
	}	
	
}
