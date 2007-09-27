package org.jastadd.plugin.jastaddj.AST;

import org.jastadd.plugin.AST.IFindDeclarationNode;

import AST.CompilationUnit;

public interface JastAddJFindDeclarationNode extends IFindDeclarationNode {
	ICompilationUnit declarationCompilationUnit();
}
