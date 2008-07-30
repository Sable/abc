package org.jastadd.plugin.jastaddj.editor.actions;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.TypeDecl;

public class FindDeclarationHandler extends JastAddActionDelegate {
	
	@Override
	public void run(IAction action) {

		IJastAddNode selectedNode = selectedNode();
		if (selectedNode instanceof IJastAddJFindDeclarationNode) {
			IJastAddNode target = ((IJastAddJFindDeclarationNode)selectedNode).declaration();
			if(target == null) 
				return;
		
			Collection<IJastAddNode> declarations = new LinkedList<IJastAddNode>();
			declarations.add(target);
		
			JastAddModel model = activeModel();
			if (model == null) 
				return;
			model.openFile(target);
			
			StringBuffer s = new StringBuffer();
			s.append("Declaration of ");
			synchronized (selectedNode.treeLockObject()) {
				if(selectedNode instanceof TypeDecl)
					s.append(((TypeDecl)selectedNode).typeName());
				else if (selectedNode instanceof IOutlineNode) {
					s.append(((IOutlineNode)selectedNode).contentOutlineLabel());
				}
			}
	
			//JastAddSearchQuery query = new JastAddSearchQuery(declarations, s.toString());
			//NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);
		}
	}
	
}
