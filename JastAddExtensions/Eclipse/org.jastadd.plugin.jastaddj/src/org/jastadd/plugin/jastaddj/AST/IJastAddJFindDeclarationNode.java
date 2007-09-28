package org.jastadd.plugin.jastaddj.AST;

import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.ISelectionNode;

public interface IJastAddJFindDeclarationNode extends ISelectionNode {
	ICompilationUnit declarationCompilationUnit();
	IJastAddNode declaration();
}
