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
import org.jastadd.plugin.JastAddModel;

import AST.ASTNode;
import AST.CompilationUnit;

public class JastAddActionDelegate implements IEditorActionDelegate {
	IEditorPart editorPart;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public void run(IAction action) {
		System.out.println("Hello");
		if(editorPart != null) {
			Object o = editorPart.getEditorInput();
			String s = null;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		Object o = selection;
		
		if(selection instanceof TextSelection) {
			TextSelection t = (TextSelection)selection;
			IEditorInput editorInput = editorPart.getEditorInput();
			if(editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
				IFile file = fileEditorInput.getFile();
				IDocument doc = JastAddDocumentProvider.fileToDocument(file);
				JastAddModel model = JastAddModel.getInstance();
				
				try {
				  int length = t.getLength();
				  int offset = t.getOffset();
				  int line = doc.getLineOfOffset(offset);
				  int column = offset - doc.getLineOffset(line);
				  
				  ASTNode node = model.findNodeInFile(file, line, column);
					if(node != null) {
						if (node.declaration() != null) {
							ASTNode target = node.declaration();
						    int targetLine = ASTNode.getLine(target.getStart());
						    while(target != null && !(target instanceof CompilationUnit))
								target = target.getParent();
							if(target instanceof CompilationUnit) {
								CompilationUnit cu = (CompilationUnit)target;
								String s1 = cu.pathName();
								String s2 = cu.relativeName();
								System.out.println("length: " + length + ", offset: " + offset + 
										", line: " + line + ", column: " + column + ", node: " + 
										node.getClass().getName() + ", target: " + 
										target.getClass().getName() + ", targetLine: " + 
										targetLine);	
							}
						}
					}
				} catch (BadLocationException e) {
				}
			}
			System.out.println(t.getOffset());
		}
	}

}
