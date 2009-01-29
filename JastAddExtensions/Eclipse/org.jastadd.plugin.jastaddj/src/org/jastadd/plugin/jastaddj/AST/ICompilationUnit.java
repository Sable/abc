package org.jastadd.plugin.jastaddj.AST;

import java.util.Collection;

import org.jastadd.plugin.compiler.ast.IJastAddNode;

public interface ICompilationUnit extends IJastAddNode {
	String pathName();
	String relativeName();
	boolean fromSource();
	Collection parseErrors();
	void errorCheck(Collection errors, Collection warnings);
	void transformation();
	void generateClassfile();
	int offset(int line, int column);
}
