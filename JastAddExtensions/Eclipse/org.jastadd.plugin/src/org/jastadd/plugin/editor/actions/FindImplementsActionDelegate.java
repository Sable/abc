package org.jastadd.plugin.editor.actions;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;

public class FindImplementsActionDelegate extends JastAddActionDelegate {
	public void run(IAction action) {
		ASTNode selectedNode = selectedNode();
		if(selectedNode != null) {
			Collection implementors = selectedNode.findImplementors();
			StringBuffer s = new StringBuffer();
			s.append("Find implementors of ");
			if(selectedNode instanceof TypeDecl)
				s.append(((TypeDecl)selectedNode).typeName());
			JastAddSearchQuery query = new JastAddSearchQuery(implementors, s.toString());
			NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);				
		}
	}
}
