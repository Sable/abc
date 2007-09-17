package org.jastadd.plugin.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.jastadd.plugin.EditorTools;

import AST.ASTNode;

public abstract class JastAddActionDelegate extends AbstractHandler implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editorPart;
	private boolean editorPartSet = false;
	private ISelection selection;
	private boolean selectionSet = false;

	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPartSet = true;
		editorPart = targetEditor;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectionSet = true;
		this.selection = selection;
	}

	public void dispose() {
		editorPartSet = false;
		selectionSet = false;
		editorPart = null;
	}

	public void init(IWorkbenchWindow window) {
		editorPartSet = true;
		editorPart = window.getActivePage().getActiveEditor();
	}

	
	// When this object acts as a command handler this method is called 
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		run(null);
		return null;
	}
	
	// When this object acts as a action this method is called
	public abstract void run(IAction action);
	
	
	protected ASTNode selectedNode() {
		return EditorTools.findNode(activeEditorPart(), activeSelection());
	}
	
	protected IEditorPart activeEditorPart() {
		if (editorPartSet && editorPart != null) 
			return editorPart;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null) return null;
		editorPart = page.getActiveEditor();
		return editorPart;
	}
	
	protected ISelection activeSelection() {
		if (selectionSet && selection != null) 
			return selection;
		IEditorSite editorSite = activeEditorPart().getEditorSite();
		if (editorPart == null) return null;
		ISelectionProvider provider = editorSite.getSelectionProvider();
		if (editorPart == null) return null;
		selection = provider.getSelection();
		return selection;
	}	
	
}
