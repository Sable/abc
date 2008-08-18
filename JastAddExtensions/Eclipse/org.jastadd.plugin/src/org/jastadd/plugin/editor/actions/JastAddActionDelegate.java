package org.jastadd.plugin.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public abstract class JastAddActionDelegate extends AbstractHandler implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IEditorPart editorPart;
	private IFile editorFile;
	private ISelection selection;

	
	// Inherited from IEditorActionDelegate - needed when actions are added to editor contexts such as the editor popup
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	// Inherited from IEditorActionDelegate via IActionDelegate - ...
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	// Inherited from IEditorActionDelegate via IActionDelegate - ...
	public abstract void run(IAction action);


	// Inherited from IWorkbenchWindowActionDelegate - needed when actions are added to the workbench menus via actionSets
	public void dispose() {
		editorPart = null;
	}

	// Inherited from IWorkbenchWindowActionDelegate - ...
	public void init(IWorkbenchWindow window) {
		editorPart = window.getActivePage().getActiveEditor();
	}

	
	// Inherited from AbstractHandler via IHandler - needed for command handlers reacting on key events
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		run(null);
		return null;
	}
	
		
	protected IJastAddNode selectedNode() {
		JastAddModel model = activeModel();
		IEditorPart editorPart = activeEditorPart();
		if (model != null && editorPart != null) {
			IEditorInput input = editorPart.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput)input;
				IFile file = fileInput.getFile();
				ISelection selection = activeSelection();
				if(selection instanceof ITextSelection && file != null) {
					return model.findNodeInDocument(model.buildFileInfo(input), ((ITextSelection)selection).getOffset());
				}
			}
			else if (input instanceof JastAddStorageEditorInput) {
				JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
				ISelection selection = activeSelection();
				if(selection instanceof ITextSelection) {
					return model.findNodeInDocument(model.buildFileInfo(input), ((ITextSelection)selection).getOffset());
				}
			}

		}
		return null;
	}
	
	protected IFile activeEditorFile() {
		IEditorInput input = editorPart.getEditorInput();
		if (!(input instanceof IFileEditorInput)) return null;
		IFileEditorInput fileInput = (IFileEditorInput)input;
		editorFile = fileInput.getFile();
		return editorFile;
	}
	
	protected IEditorPart activeEditorPart() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null) return null;
		editorPart = page.getActiveEditor();
		return editorPart;
	}
	
	protected ISelection activeSelection() {
		IEditorPart editorPart = activeEditorPart();
		if (editorPart == null) return null;
		IEditorSite editorSite = editorPart.getEditorSite();
		if (editorSite == null) return null;
		ISelectionProvider provider = editorSite.getSelectionProvider();
		if (editorPart == null) return null;
		selection = provider.getSelection();
		return selection;
	}
	
	protected JastAddEditorConfiguration activeEditorConfiguration() {
		JastAddModel model = activeModel();
		if (model != null) {
			return model.getEditorConfiguration();
		}
		return null;
	}
	
	protected JastAddModel activeModel() {
		IEditorPart part = activeEditorPart();
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			if (input != null) {
				if (input instanceof IFileEditorInput) {
					IFileEditorInput fileInput = (IFileEditorInput)input;
					IFile file = fileInput.getFile();
					return JastAddModelProvider.getModel(file);
				}
				else if (input instanceof JastAddStorageEditorInput) {
					JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
					return storageInput.getModel();
				}
			}
		}
		return null;
	}
	
}
