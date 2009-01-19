package org.jastadd.plugin.jastaddj.AST;

import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.ISelectionNode;

public interface IJastAddJFindDeclarationNode extends ISelectionNode {
	ICompilationUnit declarationCompilationUnit();
	IJastAddNode declaration();
}
