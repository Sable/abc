package org.jastadd.plugin.editor.actions;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;

import AST.ASTNode;
import AST.CompilationUnit;


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
