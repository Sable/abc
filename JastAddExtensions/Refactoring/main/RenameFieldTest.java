package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
import changes.RefactoringException;

public class RenameFieldTest extends Frontend {

	// test program for Rename Field; interactive
	public static void main(String args[]) throws Throwable {
        BufferedReader in
            = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("file name: ");
        String filename = in.readLine();
        System.out.print("class name: ");
        String classname = in.readLine();
        System.out.print("old field name: ");
        String fieldname = in.readLine();
        System.out.print("new field name: ");
        String newname = in.readLine();
        String[] a = { classname, fieldname, newname, filename };
        compile(a);
        //tst();
	}

	public static boolean compile(String args[]) {
		String classname = args[0];
		String fieldname = args[1];
		String newname = args[2];
		String[] filenames = new String[args.length-3];
		System.arraycopy(args, 3, filenames, 0, filenames.length);
		try {
			rename(classname, fieldname, newname, filenames);
		} catch(RefactoringException e) {
			System.err.println("Cannot refactor: "+e.getMessage());
		}
		return true;
	}
	
	public static void rename(String classname, String fieldname, String newname, String[] filenames) 
			throws RefactoringException {
		RenameFieldTest c = new RenameFieldTest();
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
		TypeDecl d = (TypeDecl)program.lookupType("test", path[0]);
		//System.out.println("starting at "+path[0]);
		for(int i=1;i<path.length;++i) {
			//System.out.println("next is "+path[i]);
			d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
		}
		FieldDeclaration f = (FieldDeclaration)d.localFields(fieldname).iterator().next();
		f.rename(newname);
	}

	public void output() {
		System.out.println(program);
	}

	protected String name() { return "JavaChecker"; }
	protected String version() { return "R20060915"; }
}
