package main;

import AST.BytecodeParser;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.TypeDecl;
import AST.RefactoringException;

public class ExtractClass extends Frontend {

	/*
	 * Usage: ExtractClass <file> <package> <type> <newClass> <newField> <fields> 
	 * 
	 *   <file> : the file containing the code to be refactored
	 *   <package> : the package containing <type>
	 *   <type> : the type containing <fields>
	 *   <fields> : the fields to be encapsulated separated by a space
	 *   
	 *   For an explanation of ExtractClass, see 
	 *   http://download.eclipse.org/eclipse/downloads/drops/S-3.4M1-200708091105/eclipse-news-M1.html#JDT
	 */

	public static void main(String args[]) throws Throwable {
		ExtractClass e = new ExtractClass();
		if (args.length < 6) {
			throw new IllegalArgumentException("too few arguments");
		}
		String[] fields = new String[args.length-5];
		System.arraycopy(args, 5, fields, 0, args.length-5);
		e.extractClass(args[0], args[1], args[2], fields, args[3], args[4]);
	}

	private void extractClass(String file, String pkg, String tp, String[] flds, String newClass, String newField) throws Throwable {
		JavaParser javaParser = new JavaParser() {
			public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
				return new parser.JavaParser().parse(is, fileName);
			}
		};
		String[] filenames = { file };
		if (!process(filenames, new BytecodeParser(), javaParser)) {
			throw new Exception("couldn't process input file");
		}
		String path[] = tp.split("\\.");
		TypeDecl d = (TypeDecl) program.lookupType(pkg, path[0]);
		for (int i=1; i<path.length; ++i) {
			d = (TypeDecl)d.memberTypes(path[i]).iterator().next();
		}
		ClassDecl cd = (ClassDecl) d;
		FieldDeclaration[] fieldDecls = new FieldDeclaration[flds.length];
		for(int i=0; i<flds.length; i++) {
			fieldDecls[i] = (FieldDeclaration) cd.localFields(flds[i]).iterator().next();
		}
		try {
			cd.extractClass(fieldDecls, newClass, newField);
			System.out.println(program);
		} catch(RefactoringException e) {
			System.out.println("couldn't refactor: "+e);
		}
	}

}
