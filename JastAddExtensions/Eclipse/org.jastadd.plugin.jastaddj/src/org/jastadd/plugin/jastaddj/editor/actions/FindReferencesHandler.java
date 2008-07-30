package org.jastadd.plugin.jastaddj.editor.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindReferencesNode;
import org.jastadd.plugin.search.JastAddSearchQuery;

public class FindReferencesHandler extends JastAddActionDelegate {

	@Override
	public void run(IAction action) {

		IJastAddNode selectedNode = selectedNode();
		if(selectedNode instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode node = (IJastAddJFindDeclarationNode)selectedNode;
			IJastAddNode target = node.declaration();
			if(target instanceof IJastAddJFindReferencesNode) {
				IJastAddJFindReferencesNode decl = (IJastAddJFindReferencesNode)target;
				Collection references = new ArrayList();
				StringBuffer s = new StringBuffer();
				s.append("References of ");
				synchronized (node.treeLockObject()) {
					references = decl.references();
					if(node instanceof IOutlineNode) {
						s.append(((IOutlineNode)node).contentOutlineLabel());
					}
				}
				JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
				NewSearchUI.runQueryInBackground((ISearchQuery)query);				
			}
		}
	}
}
