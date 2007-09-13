package org.jastadd.plugin.editor.actions;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;

public class FindReferencesActionDelegate extends JastAddActionDelegate {
	public void run(IAction action) {
		ASTNode selectedNode = selectedNode();
		if(selectedNode != null) {
			Collection references = selectedNode.findReferences();
			StringBuffer s = new StringBuffer();
			s.append("Find references of ");
			if(selectedNode instanceof TypeDecl)
				s.append(((TypeDecl)selectedNode).typeName());
			JastAddSearchQuery query = new JastAddSearchQuery(references, s.toString());
			NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);				
		}
	}
}
