package org.jastadd.plugin.jastaddj.AST;

import java.util.Collection;

import org.jastadd.plugin.compiler.ast.ISelectionNode;

public interface IJastAddJFindImplementsNode extends ISelectionNode {

	Collection implementors();

}
