package org.jastadd.plugin;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;

import AST.ASTNode;
import AST.CompilationUnit;
import AST.JavaParser;
import AST.LexicalError;
import AST.ParseError;
import AST.Program;


public class JastAddModel {

	private static JastAddModel instance = null;
	
	public JastAddModel() {
		if (instance == null) {
		  JastAddModel.instance = this;
		}
	}
	
	public static JastAddModel getInstance() {
		return JastAddModel.instance;
	}
	
	public ASTNode findNodeInFile(IFile file, int line, int column) {
		if(file == null)
			return null;
		Program program = new Program();
		program.initBytecodeReader(new bytecode.Parser());
		program.initJavaParser(
				new JavaParser() {
					public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
						return new parser.JavaParser().parse(is, fileName);
					}
				}
		);
		program.initPackageExtractor(new scanner.JavaScanner());
		program.initOptions();
		program.addKeyValueOption("-classpath");
		program.addOptions(new String[] { file.getRawLocation().toOSString() });
		Collection files = program.files();
		try {
			for(Iterator iter = files.iterator(); iter.hasNext(); ) {
				String name = (String)iter.next();
				program.addSourceFile(name);
			}

			for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
				CompilationUnit unit = (CompilationUnit)iter.next();
				if(unit.fromSource()) {
					ASTNode node = findLocation(unit, line+1, column+1);
					if(node != null)
						return node;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private ASTNode findLocation(ASTNode node, int line, int column) {
		int beginLine = ASTNode.getLine(node.getStart());
		int beginColumn = ASTNode.getColumn(node.getStart());
		int endLine = ASTNode.getLine(node.getEnd());
		int endColumn = ASTNode.getColumn(node.getEnd());

		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				ASTNode result = findLocation(node.getChild(i), line, column);
				if(result != null)
					return result;
			}
			return null;
		}
		
		if((line >= beginLine && line <= endLine) &&
		   (line == beginLine && column >= beginColumn || line != beginLine) &&
		   (line == endLine && column <= endColumn || line != endLine)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				ASTNode result = findLocation(node.getChild(i), line, column);
				if(result != null)
					return result;
			}
			return node;
		}
		return null;
	}
}