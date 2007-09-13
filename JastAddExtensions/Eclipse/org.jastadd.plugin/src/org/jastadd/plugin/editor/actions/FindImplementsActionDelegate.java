package org.jastadd.plugin.editor.actions;

import java.util.Collection;

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
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.search.JastAddSearchQuery;

import AST.ASTNode;
import AST.TypeDecl;

public class FindImplementsActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {

	private IEditorPart editorPart;
	private ASTNode selectedNode;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public void run(IAction action) {
		if (editorPart != null) {
			
			if (selectedNode != null) {
				
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

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void init(IWorkbenchWindow window) {
		editorPart = window.getActivePage().getActiveEditor();
	}
}
