package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

import tests.TestHelper;
import AST.ASTNode;
import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.FileRange;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.TypeDecl;
import AST.RefactoringException;

public class RenameField extends Frontend {

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
		String path[] = classname.split("\\.");
		Program program = c.program;
		TypeDecl d = (TypeDecl)program.lookupType(pkgname, path[0]);
		//System.out.println("starting at "+path[0]);
		for(int i=1;i<path.length;++i) {
			//System.out.println("next is "+path[i]);
			d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
		}
		FieldDeclaration f = (FieldDeclaration)d.localFields(fieldname).iterator().next();
		f.rename(newname);
	}

	protected void processWarnings(Collection errors, CompilationUnit unit) { }
	public void output() { }
}
