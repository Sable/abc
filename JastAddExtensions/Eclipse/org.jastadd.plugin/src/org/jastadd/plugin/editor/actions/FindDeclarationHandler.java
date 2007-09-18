package org.jastadd.plugin.editor.actions;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;

public class FindDeclarationHandler extends JastAddActionDelegate {
	
	@Override
	public void run(IAction action) {

		ASTNode selectedNode = selectedNode();
		if (selectedNode == null) return;
		
		ASTNode target = selectedNode.declaration();
		if(target == null) return;
		
		Collection<ASTNode> declarations = new LinkedList<ASTNode>();
		declarations.add(target);
		EditorTools.openFile(target);
		
		StringBuffer s = new StringBuffer();
		s.append("Find declaration of ");
		if(selectedNode instanceof TypeDecl)
			s.append(((TypeDecl)selectedNode).typeName());
		else s.append(selectedNode.contentOutlineLabel());
		
		JastAddSearchQuery query = new JastAddSearchQuery(declarations, s.toString());
		NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);	
	}
	
}
