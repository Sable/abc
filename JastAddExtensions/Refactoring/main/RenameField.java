package main;

import java.util.Collection;

import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class RenameField extends Frontend {

	/*
	 * Usage: RenameField <package> <type> <field> <new name> <file>...
	 * 
	 *   <package> : the package containing <type>
	 *   <type> : the type containing <field>
	 *   <field> : the field to be renamed
	 *   <new name> : the new name to rename it to
	 *   <file>... : the files containing the code to be refactored
	 */

	public static void main(String args[]) throws Throwable {
		String pkgname = args[0];
		String classname = args[1];
		String fieldname = args[2];
		String newname = args[3];
		String[] filenames = new String[args.length-4];
		System.arraycopy(args, 4, filenames, 0, filenames.length);
		try {
			rename(pkgname, classname, fieldname, newname, filenames);
		} catch(RefactoringException e) {
			System.err.println("Cannot refactor: "+e.getMessage());
		}
	}
	
	public static void rename(String pkgname, String classname, String fieldname, String newname, String[] filenames) 
			throws RefactoringException {
	    //long time = System.currentTimeMillis();
		RenameField c = new RenameField();
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
		//time = System.currentTimeMillis();
		//for(int cnt=0;cnt<RenameType.NUM_RUNS;cnt++) {
			String path[] = classname.split("\\.");
			Program program = c.program;
			TypeDecl d = (TypeDecl)program.lookupType(pkgname, path[0]);
			for(int i=1;i<path.length;++i)
				d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
			FieldDeclaration f = (FieldDeclaration)d.memberFields(fieldname).iterator().next();
			f.rename(newname);
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
