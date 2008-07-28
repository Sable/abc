package org.jastadd.plugin.jastadd;


import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastadd.AST.IJastAddFindEquationsNode;
import org.jastadd.plugin.search.JastAddSearchQuery;

public class FindEquationsHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {

		IJastAddNode selectedNode = selectedNode();
		if(selectedNode instanceof IJastAddFindEquationsNode) {
			IJastAddFindEquationsNode node = (IJastAddFindEquationsNode)selectedNode;
			Collection equations = new ArrayList();
			StringBuffer s = new StringBuffer();
			s.append("Find references of ");
			synchronized (((IJastAddNode)node).treeLockObject()) {
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
