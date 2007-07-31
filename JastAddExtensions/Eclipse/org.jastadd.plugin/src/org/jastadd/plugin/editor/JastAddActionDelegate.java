package org.jastadd.plugin.editor;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
	
	private IEditorPart editorPart;
	private ASTNode selectedNode;

	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
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
					System.out.println(t.getOffset() + ", line: " + line + ", col: " + column);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			
		}
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
						   openFile(unit, targetLine, targetColumn, targetLength);
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
	
	
	
	private void openFile(CompilationUnit unit, int line, int column, int length) {
		if (editorPart == null)
			return;

		String pathName = unit.pathName();
		if (pathName.endsWith(".class")) {
			pathName = pathName.replace(".class", ".java");
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
		IPath path = URIUtil.toPath(new URI("file:/" + pathName));
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
		IPath path = URIUtil.toPath(new URI("file:/" + pathName));
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
