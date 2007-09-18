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

	
	// Inherited from IEditorActionDelegate - needed when actions are added to editor contexts such as the editor popup
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPartSet = true;
		editorPart = targetEditor;
	}

	// Inherited from IEditorActionDelegate via IActionDelegate - ...
	public void selectionChanged(IAction action, ISelection selection) {
		selectionSet = true;
		this.selection = selection;
	}
	
	// Inherited from IEditorActionDelegate via IActionDelegate - ...
	public abstract void run(IAction action);


	// Inherited from IWorkbenchWindowActionDelegate - needed when actions are added to the workbench menus via actionSets
	public void dispose() {
		editorPartSet = false;
		selectionSet = false;
		editorPart = null;
	}

	// Inherited from IWorkbenchWindowActionDelegate - ...
	public void init(IWorkbenchWindow window) {
		editorPartSet = true;
		editorPart = window.getActivePage().getActiveEditor();
	}

	
	// Inherited from AbstractHandler via IHandler - needed for command handlers reacting on key events
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		System.out.println("Keyevent");
		run(null);
		return null;
	}
	
	
	
	protected ASTNode selectedNode() {
		return EditorTools.findNode(activeEditorPart(), activeSelection());
	}
	
	protected IEditorPart activeEditorPart() {
//		if (editorPartSet && editorPart != null) 
//			return editorPart;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null) return null;
		editorPart = page.getActiveEditor();
		return editorPart;
	}
	
	protected ISelection activeSelection() {
//		if (selectionSet && selection != null) 
//			return selection;
		IEditorSite editorSite = activeEditorPart().getEditorSite();
		if (editorPart == null) return null;
		ISelectionProvider provider = editorSite.getSelectionProvider();
		if (editorPart == null) return null;
		selection = provider.getSelection();
		return selection;
	}	
	
}
