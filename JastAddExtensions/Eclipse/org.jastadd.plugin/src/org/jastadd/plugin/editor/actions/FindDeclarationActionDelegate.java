package org.jastadd.plugin.editor.actions;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;


public class FindDeclarationActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editorPart;
	private ASTNode selectedNode;

	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			IEditorInput editorInput = editorPart.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
				IFile file = fileEditorInput.getFile();
				selectedNode = JastAddModel.getInstance().findNodeInDocument(file, textSelection.getOffset());
			}
		}
	}

	public void run(IAction action) {
		if (editorPart != null) {
			
			if (selectedNode != null) {
				// Find the file and position of the declaration node
				ASTNode target = selectedNode.declaration();
				if(target != null) {
					Collection declarations = new LinkedList();
					declarations.add(target);
					EditorTools.openFile(target);
					StringBuffer s = new StringBuffer();
					s.append("Find declaration of ");
					if(selectedNode instanceof TypeDecl)
						s.append(((TypeDecl)selectedNode).typeName());
					JastAddSearchQuery query = new JastAddSearchQuery(declarations, s.toString());
					NewSearchUI.runQueryInForeground(null, (ISearchQuery)query);
				}
			}
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		editorPart = window.getActivePage().getActiveEditor();
	}
	
}
