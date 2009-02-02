package org.jastadd.plugin.jastadd.editor.actions;


import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastadd.AST.IJastAddFindEquationsNode;
import org.jastadd.plugin.search.JastAddSearchQuery;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;

import org.jastadd.plugin.compiler.ast.IOutlineNode;

public class FindEquationsHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {

		IJastAddNode selectedNode = selectedNode();
		if(selectedNode instanceof IJastAddFindEquationsNode) {
			IJastAddFindEquationsNode node = (IJastAddFindEquationsNode)selectedNode;
			Collection equations = new ArrayList();
			StringBuffer s = new StringBuffer();
			s.append("Find references of ");
			synchronized (((IASTNode)node).treeLockObject()) {
				equations = node.equations();
				if(node instanceof IOutlineNode) {
					s.append(((IOutlineNode)node).contentOutlineLabel());
				}
			}
			JastAddSearchQuery query = new JastAddSearchQuery(equations, s.toString());
			NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);
		}
	}
}
