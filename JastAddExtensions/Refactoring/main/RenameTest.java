package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import AST.ASTNode;
import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.Frontend;
import AST.JavaParser;
import AST.Program;
import AST.TypeDecl;
import changes.ASTChange;
import changes.RefactoringException;

public class RenameTest extends Frontend {

	public static void main(String args[]) throws Throwable {
        /*BufferedReader in
            = new BufferedReader(new InputStreamReader(System.in));        
        System.out.print("class name: TestSrc.");
        String classname = "TestSrc."+in.readLine();
        System.out.print("old field name: ");
        String fieldname = in.readLine();
        System.out.print("new field name: ");
        String newname = in.readLine();
        String[] a = { classname, fieldname, newname, "test/TestSrc.java" };
        compile(a);*/
        tst();
	}

	public static boolean compile(String args[]) {
		String classname = args[0];
		String fieldname = args[1];
		String newname = args[2];
		String[] filenames = new String[args.length-3];
		System.arraycopy(args, 3, filenames, 0, filenames.length);
		try {
			//System.out.println("rename("+classname+", "+fieldname+", "+newname+", {"+filenames[0]+"})");

			List changes = rename(classname, fieldname, newname, filenames);
			System.out.println("proposed changes: ");
			for(Iterator i=changes.iterator();i.hasNext();) {
				ASTChange c = (ASTChange)i.next();
				System.out.println(c.prettyprint());
			}
		} catch(RefactoringException e) {
			System.err.println("Cannot refactor: "+e.getMessage());
		}
		return true;
	}
	
	public static void tst() throws RefactoringException {
		RenameTest c = new RenameTest();
		String[] filenames = { "test/Tmp.java" };
		if(!c.process(filenames, new BytecodeParser(), 
				new JavaParser() {
			public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
				return new parser.JavaParser().parse(is, fileName);
			}
		}
		))
			throw new RefactoringException("couldn't process input files");
		Program program = c.program;
        if(1 == 1) {
            System.out.println(program);
            return;
        }
        ASTNode n = RunAccessTests.findSmallestCoveringNode(program, filenames[0], new FileRange(59, 17, 59, 21));
        if(!(n instanceof FieldDeclaration))
            throw new RefactoringException("nothing there except "+n);
		try {
			List changes = ((FieldDeclaration)n).rename("u");
			System.out.println("proposed changes: ");
			for(Iterator i=changes.iterator();i.hasNext();) {
				ASTChange ch = (ASTChange)i.next();
				System.out.println(ch.prettyprint());
			}
		} catch(RefactoringException e) {
			System.err.println("Cannot refactor: "+e.getMessage());
		}
	}

	public static List rename(String classname, String fieldname, String newname, String[] filenames) 
			throws RefactoringException {
		RenameTest c = new RenameTest();
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
		return f.rename(newname);
	}

	public void output() {
		System.out.println(program);
	}

	protected String name() { return "JavaChecker"; }
	protected String version() { return "R20060915"; }
}
