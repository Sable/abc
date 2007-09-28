package org.jastadd.plugin.jastaddj.AST;

import java.util.Collection;

import org.jastadd.plugin.AST.ISelectionNode;

public interface IJastAddJFindReferencesNode extends ISelectionNode {
    Collection references();
}
