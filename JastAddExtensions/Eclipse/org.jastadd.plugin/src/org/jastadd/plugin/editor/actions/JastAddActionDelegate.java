package org.jastadd.plugin.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jastadd.plugin.EditorTools;

import AST.ASTNode;

public abstract class JastAddActionDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editorPart;
	private ISelection selection;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public abstract void run(IAction action);
	
	protected ASTNode selectedNode() {
		return EditorTools.findNode(editorPart, selection);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void dispose() {
		editorPart = null;
	}

	public void init(IWorkbenchWindow window) {
		editorPart = window.getActivePage().getActiveEditor();
	}

}
