package org.jastadd.plugin.jastaddj.AST;

import java.util.Collection;
import java.util.Iterator;

import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.compiler.recovery.*;

public interface IProgram extends IJastAddNode {
	Iterator compilationUnitIterator();

	Collection files();

	void flushSourceFiles(Collection changedFileNames);
	
	public void addKeyValueOption(String name);	

	void initOptions();

	void addOptions(String[] options);

	void addSourceFile(String name);

	void addSourceFile(String fileName, String contents);
	
	IOutlineNode[] mainTypes();
	
	ICompilationUnit addSourceFileWithRecovery(String fileName, String contents, RecoveryLexer lexer) throws Exception;

}
