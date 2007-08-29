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
					int targetLine = target.declarationLocationLine();
					int targetColumn = target.declarationLocationColumn();
					int targetLength = target.declarationLocationLength();
					CompilationUnit cu = target.declarationCompilationUnit();
					if(cu != null)
						openFile(cu, targetLine, targetColumn, targetLength);
					else
						System.out.println("Cannot find CompilationUnit");
				}
				System.out.println("Node does not bind to a declaration");
			}
		}
	}
	
	
	
	private void openFile(CompilationUnit unit, int line, int column, int length) {
		if (editorPart == null)
			return;

		String pathName = unit.pathName();
		if (pathName.endsWith(".class")) {
			pathName = pathName.replace(".class", ".java");
		} 
		else if(pathName.endsWith(JastAddModel.DUMMY_SUFFIX)) {
			pathName = pathName.substring(0, pathName.length() - JastAddModel.DUMMY_SUFFIX.length());
		}
		
		IEditorInput editorInput = editorPart.getEditorInput();
		
		if (editorInput instanceof IFileEditorInput) {
			boolean finishedTrying = false;
			while (!finishedTrying) {

				if (pathName.endsWith(".java")) {
				
					try {
					  openJavaFile(pathName, line, column, length);
					  finishedTrying = true;
				    } catch (PartInitException e) {
					  finishedTrying = true;
				    } catch (URISyntaxException e1) {
					  if (pathName.endsWith(".java")) {
					    pathName = pathName.replace(".java", ".class");
					  }
				    }
				    
				} else if (pathName.endsWith(".class") || pathName.endsWith(".jar")) {

					try {
						openClassFile(pathName, unit.relativeName(), pathName.endsWith(".jar"), line, column, length);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					} catch (PartInitException e) {
						e.printStackTrace();
					}	
					finishedTrying = true;
				}
			}
		}
	}
		
	private void openJavaFile(String pathName, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length >= 1) {
			IEditorInput targetEditorInput = new FileEditorInput(files[0]);
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			page.openEditor(targetEditorInput,
					"org.jastadd.plugin.editor.JastAddEditor", true);

			IDocument targetDoc = JastAddDocumentProvider
					.fileToDocument(files[0]);
			int lineOffset = 0;
			try {
				lineOffset = targetDoc.getLineOffset(line - 1) + column - 1;
			} catch (BadLocationException e) {
			}

			IEditorPart targetEditorPart = page.findEditor(targetEditorInput);
			if (targetEditorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) targetEditorPart;
				textEditor.selectAndReveal(lineOffset, length);
			}
		}
	}

	private void openClassFile(String pathName, String relativeName,
			boolean inJarFile, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length > 0) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench()
					.getEditorRegistry().getDefaultEditor(files[0].getName());
			page.openEditor(new FileEditorInput(files[0]), desc.getId());

			// page.openEditor(targetFileEditorInput,"org.eclipse.jdt.ui.ClassFileEditor",
			// false);
			// "org.eclipse.jdt.ui.CompilationUnitEditor",
			// false);
		}
	}
}
