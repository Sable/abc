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
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.resources.JastAddDocumentProvider;

import AST.ASTNode;
import AST.CompilationUnit;

/**
 * Collection of useful editor operations
 */
public class EditorTools {

	private static final String JAVA_FILE_EXT = ".java";
	private static final String JAR_FILE_EXT = ".jar";
	private static final String CLASS_FILE_EXT = ".class";
	
	/**
	 * Sets the position in the active editor to the given offset.
	 * @param offset The editor offset.
	 */
	public static void setActiveEditorPosition(int offset) {
		setActiveEditorPosition(offset, 0);
	}
	
	/**
	 * Sets the given offset and length in the active editor.
	 * @param offset The editor offset
	 * @param length The selection length
	 */
	public static void setActiveEditorPosition(int offset, int length) {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		IEditorPart editorPart = page.getActiveEditor();
		if (editorPart instanceof ITextEditor) {			
			ITextEditor textEditor = (ITextEditor) editorPart;
			textEditor.selectAndReveal(offset, 1);
		}
 	}
	
	/**
	 * Locates the abstract syntax node corresponding to the given selection
	 * @param editorPart The editor part related to the selection
	 * @param selection The selection, which should be a text selection
	 * @return A corresponding ASTNode object or null if no match was found
	 */
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
	

	/**
	 * Opens the file from which the given ASTNode origins
	 * @param node The node to look up
	 */
	public static void openFile(ASTNode node) {
		int targetLine = node.declarationLocationLine();
		int targetColumn = node.declarationLocationColumn();
		int targetLength = node.declarationLocationLength();
		CompilationUnit cu = node.declarationCompilationUnit();
		openFile(cu, targetLine, targetColumn, targetLength);
	}
	
	/**
	 * Opens the file corresponding to the given compilation unit with a selection
	 * corresponding to the given line, column and length.
	 * @param unit The compilation unit to open.
	 * @param line The line on which to start the selection
	 * @param column The column on which to start the selection
	 * @param length The length of the selection
	 */
	private static void openFile(CompilationUnit unit, int line, int column, int length) {
		String pathName = unit.pathName();
		if (pathName.endsWith(CLASS_FILE_EXT)) {
			pathName = pathName.replace(CLASS_FILE_EXT, JAVA_FILE_EXT);
		} 
		boolean finishedTrying = false;
		while (!finishedTrying) {
			if (pathName.endsWith(JAVA_FILE_EXT)) {

				try {
					openJavaFile(pathName, line, column, length);
					finishedTrying = true;
				} catch (PartInitException e) {
					finishedTrying = true;
				} catch (URISyntaxException e1) {
					if (pathName.endsWith(JAVA_FILE_EXT)) {
						pathName = pathName.replace(JAVA_FILE_EXT, CLASS_FILE_EXT);
					}
				}

			} else if (pathName.endsWith(CLASS_FILE_EXT) || pathName.endsWith(JAR_FILE_EXT)) {

				try {
					openClassFile(pathName, unit.relativeName(), pathName.endsWith(JAR_FILE_EXT), line, column, length);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (PartInitException e) {
					e.printStackTrace();
				}	
				finishedTrying = true;
			}
		}
	}
		
	/**
	 * Opens the file corresponding to the given pathName with a selection corresponding
	 * to line, column and length. 
	 * @param pathName Path to a java file
	 * @param line Line on which to start the selection
	 * @param column Column on which to start the selection 
	 * @param length The length of the selection
	 * @throws PartInitException Thrown when somethings wrong with the path
	 * @throws URISyntaxException Thrown when somethings wrong with the URI syntax of the path
	 */
	private static void openJavaFile(String pathName, int line, int column, int length)
			throws PartInitException, URISyntaxException {
		IPath path = Path.fromOSString(pathName);//URIUtil.toPath(new URI("file:/" + pathName));
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocation(path);
		if (files.length >= 1) {
			IEditorInput targetEditorInput = new FileEditorInput(files[0]);
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			page.openEditor(targetEditorInput, JastAddEditor.ID, true);

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

	/**
	 * Opens a class corresponding to the given path name and makes a selection
	 * corresponding to line, column and length.
	 * @param pathName The path to the class
	 * @param relativeName The relative name of the class
	 * @param inJarFile true if the class resides in a jar file
	 * @param line Line on which to start selection
	 * @param column Column on which to start selection
	 * @param length The length of the selection
	 * @throws PartInitException Thrown if somethings wrong with the path
	 * @throws URISyntaxException Thrown if somethings wrong with the URI syntax of the path
	 */
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
