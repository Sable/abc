package org.jastadd.plugin.editor;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.MarkSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
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
		if (editorPart != null) {
			
			if (selectedNode != null) {
				if (selectedNode.declaration() != null) {
					// Find the file and position of the declaration node
					ASTNode target = selectedNode.declaration();
					int targetLine = ASTNode.getLine(target.getStart());
					int targetColumn = ASTNode.getColumn(target.getStart());
					int targetLength = ASTNode.getColumn(target.getEnd()) - targetColumn + 1;

					while (target != null && !(target instanceof CompilationUnit))
						target = target.getParent();

					if (target instanceof CompilationUnit) {
						CompilationUnit unit = (CompilationUnit) target;
						String relativeName = unit.relativeName();
						if (relativeName != null) {
						   openFileFromSource(unit, targetLine, targetColumn, targetLength);
						} else {
							System.out.println("Unknown declaration");
						}
						
					} else {
						System.out.println("Cannot find CompilationUnit");
					}
				} else {
					System.out.println("Unknown declaration");
				}
			} else {
				System.out.println("Unkown construct");
			}
		}
	}
/*
	private void openByteFile(CompilationUnit unit, int line, int column, int length) {
		String relativeName = unit.relativeName();
		String path = unit.pathName();
		IEditorInput editorInput = editorPart.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			try {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;

				String fullPathString = "file://" + path;
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new URI(fullPathString));
				if (files.length == 1) {
					IFile file = files[0];
					String targetFileData = file.getName();
					FileEditorInput targetFileEditorInput = new FileEditorInput(files[0]);
					
					IWorkbench workbench = PlatformUI.getWorkbench();
					//IEditorRegistry editorRegistry = workbench.getEditorRegistry();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					   IEditorDescriptor desc = PlatformUI.getWorkbench().
					      getEditorRegistry().getDefaultEditor(file.getName());
					   page.openEditor(
					      new FileEditorInput(file),
					      desc.getId());
					
					//page.openEditor(targetFileEditorInput,"org.eclipse.jdt.ui.ClassFileEditor", false);
							//"org.eclipse.jdt.ui.CompilationUnitEditor", false);
							
				}
			} catch (URISyntaxException e) { 
			//} catch (IOException e) {
			//	System.out.println(e.getMessage());
			} catch (PartInitException e) {
				System.out.println(e.getMessage());
			//} catch (CoreException e) {
			//	System.out.println(e.getMessage());
			}
		}
	}
	
	
	protected IEditorInput transformEditorInput(IEditorInput input) {

		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			IClassFileEditorInput classFileInput= new ExternalClassFileEditorInput(file);
			if (classFileInput.getClassFile() != null)
				input= classFileInput;
		}

		return input;
	}
	*/
	
	private void openFileFromSource(CompilationUnit unit, int line, int column, int length) {	
		if (editorPart == null)
			return;
		
		String relativeName = unit.relativeName();
		String destPathName = unit.destinationPath();
		String pathName = unit.pathName();
		String packageName = unit.packageName();

		int startIndex = relativeName.lastIndexOf('/');
		if (startIndex > 0) {
			relativeName = relativeName.substring(startIndex + 1);
		}
		if (relativeName.endsWith(".class")) {
			relativeName = relativeName.replaceFirst(".class", ".java");
		}
		
		IEditorInput editorInput = editorPart.getEditorInput();

		if (editorInput instanceof IFileEditorInput) {

			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			IFile targetFile = fileEditorInput.getFile().getProject().getFile(relativeName);
			IEditorInput targetEditorInput = new FileEditorInput(targetFile);
			
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	        IWorkbenchPage page = window.getActivePage();
			try {
				page.openEditor(targetEditorInput,
						"org.jastadd.plugin.editor.JastAddEditor", true);
				
				IDocument targetDoc = JastAddDocumentProvider.fileToDocument(targetFile);
				int lineOffset = 0;
				try {
				  lineOffset = targetDoc.getLineOffset(line-1) + column-1;
				} catch (BadLocationException e) {}
				
				
				IEditorPart targetEditorPart = page
						.findEditor(targetEditorInput);
				if (targetEditorPart instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor)targetEditorPart;
					textEditor.selectAndReveal(lineOffset, length);
				}
			} catch (PartInitException e) {
			}
	    }
	}
	

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection t = (TextSelection) selection;
			IEditorInput editorInput = editorPart.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
				IFile file = fileEditorInput.getFile();
				IDocument doc = JastAddDocumentProvider.fileToDocument(file);
				JastAddModel model = JastAddModel.getInstance();

				try {
					int offset = t.getOffset();
					int line = doc.getLineOfOffset(offset);
					int column = offset - doc.getLineOffset(line);
					selectedNode = model.findNodeInFile(file, line, column);
				} catch (BadLocationException e) {
				}
			}
			System.out.println(t.getOffset());
		}
	}

}
