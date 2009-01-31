package org.jastadd.plugin.jastaddj.editor.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindImplementsNode;
import org.jastadd.plugin.search.JastAddSearchQuery;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;

public class FindImplementsHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		
		IJastAddNode selectedNode = selectedNode();
		if(selectedNode instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode node = (IJastAddJFindDeclarationNode)selectedNode;
			IJastAddNode target = node.declaration();
			if(target instanceof IJastAddJFindImplementsNode) {
				IJastAddJFindImplementsNode decl = (IJastAddJFindImplementsNode)target;
				Collection references = new ArrayList();
				StringBuffer s = new StringBuffer();
				s.append("Implementors of ");
				synchronized (((IASTNode)node).treeLockObject()) {
					references = decl.implementors();
					if(node instanceof IOutlineNode) {
						s.append(((IOutlineNode)node).contentOutlineLabel());
					}
				}
				JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
				NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);				
			}
		}
	}

}
