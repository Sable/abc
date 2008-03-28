package main;

import java.util.Collection;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.Frontend;
import AST.JavaParser;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameMethod extends Frontend {

	/*
	 * Usage: RenameMethod <package> <type> <method> <new name> <file>...
	 * 
	 *   <package> : the package containing <type>
	 *   <type> : the type containing <field>
	 *   <field> : the method to be renamed
	 *   <new name> : the new name to rename it to
	 *   <file>... : the files containing the code to be refactored
	 */

	public static void main(String args[]) throws Throwable {
		String pkgname = args[0];
		String classname = args[1];
		String methoddname = args[2];
		String newname = args[3];
		String[] filenames = new String[args.length-4];
		System.arraycopy(args, 4, filenames, 0, filenames.length);
		try {
			rename(pkgname, classname, methoddname, newname, filenames);
		} catch(RefactoringException e) {
			System.err.println("Cannot refactor: "+e.getMessage());
		}
	}
	
	public static void rename(String pkgname, String classname, String methodname, String newname, String[] filenames) 
			throws RefactoringException {
	    //long time = System.currentTimeMillis();
		RenameMethod c = new RenameMethod();
		if(!c.process(
				filenames,
				new BytecodeParser(),
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				}
		))
			throw new RefactoringException("couldn't process input files");
		//System.out.println("time to load: "+(System.currentTimeMillis()-time)+"\n");
		//for(int cnt=0;cnt<RenameType.NUM_RUNS;cnt++) {
		//time = System.currentTimeMillis();
			String path[] = classname.split("\\.");
			Program program = c.program;
			TypeDecl d = (TypeDecl)program.lookupType(pkgname, path[0]);
			for(int i=1;i<path.length;++i)
				d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
			MethodDecl m = (MethodDecl)d.memberMethods(methodname).iterator().next();
			m.rename(newname);
			System.out.println(program);
			//System.out.println("total: "+(System.currentTimeMillis()-time));
			//if(cnt == 0 && RenameType.hasErrors(program))
			//System.err.println("Output program has errors!");
			//time = System.currentTimeMillis();
			//program.undo();
			//System.out.println("undo: "+(System.currentTimeMillis()-time)+"\n");
			//program.flushCaches();
			//}
	}

	protected void processWarnings(Collection errors, CompilationUnit unit) { }
	public void output() { }
}
