package org.jastadd.plugin.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.jastadd.plugin.JastAddModel;

import AST.ASTNode;
import AST.CompilationUnit;

public class JastAddActionDelegate implements IEditorActionDelegate {
	IEditorPart editorPart;
	ASTNode selectedNode;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public void run(IAction action) {
		if(editorPart != null) {
			if (selectedNode != null) {
				if (selectedNode.declaration() != null) {
					// Find the file and position of the declaration node 
					ASTNode target = selectedNode.declaration();
				    int targetLine = ASTNode.getLine(target.getStart());
				    int column = ASTNode.getColumn(target.getStart());
				    
				    while(target != null && !(target instanceof CompilationUnit))
						target = target.getParent();
					
				    if(target instanceof CompilationUnit) {
						CompilationUnit cu = (CompilationUnit)target;
						String relativeName = cu.relativeName();
						// The path to the class file should be the path to the source file 
						if (relativeName.endsWith(".class")) {
							relativeName = relativeName.replaceFirst(".class", ".java");
						}
						
						try {
							
						  IEditorInput editorInput = editorPart.getEditorInput();
						  
						  if (editorInput instanceof IFileEditorInput) {
							IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
							IFile targetFile = fileEditorInput.getFile().getProject().getFile(relativeName);
						    IEditorInput targetEditorInput = new FileEditorInput(targetFile);
						    String path = targetFile.getRawLocation().toOSString();

						    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						    IWorkbenchPage page = window.getActivePage();
						    page.openEditor(targetEditorInput, "org.jastadd.plugin.editor.JastAddEditor", false);
						  }
						  
						  // Start an editor with the right file position
						  /*
						  editorPart.getSite().getPage().openEditor(editorPart.getEditorInput(), 
								"org.jastadd.plugin.editor.JastAddEditor", true);
								*/
						  
						} catch (PartInitException e) { }
						
						
						System.out.println("selectedNode: " + selectedNode.getClass().getName() + 
								", target class: " + target.getClass().getName() + ", targetLine: " + 
								targetLine + ", relativeName: " + relativeName);
						
					} else System.out.println("Can't find the declaration");
				} else System.out.println("Unknown declaration");
			} else System.out.println("Unkown construct");
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof TextSelection) {
			TextSelection t = (TextSelection)selection;
			IEditorInput editorInput = editorPart.getEditorInput();
			if(editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
				IFile file = fileEditorInput.getFile();
				IDocument doc = JastAddDocumentProvider.fileToDocument(file);
				JastAddModel model = JastAddModel.getInstance();
				
				try {
				  int offset = t.getOffset();
				  int line = doc.getLineOfOffset(offset);
				  int column = offset - doc.getLineOffset(line);
				  selectedNode = model.findNodeInFile(file, line, column);
				} catch (BadLocationException e) {}
			}
			System.out.println(t.getOffset());
		}
	}

}
