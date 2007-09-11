package org.jastadd.plugin.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.JastAddModel;

import AST.ASTNode;


public class FindDeclarationActionDelegate implements IEditorActionDelegate {
	
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
					EditorTools.openFile(target);
				}
			}
		}
	}
	
}
