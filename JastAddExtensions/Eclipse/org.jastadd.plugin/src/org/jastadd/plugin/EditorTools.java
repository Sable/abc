package org.jastadd.plugin;

import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
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

import AST.ASTNode;
import AST.CompilationUnit;

public class EditorTools {

	public static void setActiveEditorPosition(int offset) {
		setActiveEditorPosition(offset, 0);
	}
	
	public static void setActiveEditorPosition(int offset, int length) {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		IEditorPart editorPart = page.getActiveEditor();
		if (editorPart instanceof ITextEditor) {			
			ITextEditor textEditor = (ITextEditor) editorPart;
			textEditor.selectAndReveal(offset, 1);
		}
 	}
	
	public static ASTNode findNode(IEditorPart editorPart, ISelection selection) {
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput)editorPart.getEditorInput();
			IFile file = fileEditorInput.getFile();
			if(selection instanceof ITextSelection && file != null) {
				return JastAddModel.getInstance().findNodeInDocument(file, ((ITextSelection)selection).getOffset());
			}
		}
		return null;
	}
	

	
	public static void openFile(ASTNode node) {
		int targetLine = node.declarationLocationLine();
		int targetColumn = node.declarationLocationColumn();
		int targetLength = node.declarationLocationLength();
		CompilationUnit cu = node.declarationCompilationUnit();
		openFile(cu, targetLine, targetColumn, targetLength);
	}
	
	private static void openFile(CompilationUnit unit, int line, int column, int length) {
		String pathName = unit.pathName();
		if (pathName.endsWith(".class")) {
			pathName = pathName.replace(".class", ".java");
		} 
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
		
	private static void openJavaFile(String pathName, int line, int column, int length)
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

	private static void openClassFile(String pathName, String relativeName,
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
