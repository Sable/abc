package org.jastadd.plugin.jastaddj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ICompiler;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.jastaddj.AST.IJastAddJFindDeclarationNode;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;
import org.jastadd.plugin.util.FileInfo;
import org.jastadd.plugin.util.FileInfoMap;
import org.jastadd.plugin.util.JastAddStorageEditorInput;

public class FileUtil {
	
	public static IFile getFile(IJastAddNode node) {
		synchronized(((IASTNode)node).treeLockObject()) {
			IJastAddNode root = node;
			while (root != null && !(root instanceof ICompilationUnit))
				root = root.getParent();
			if (root == null)
				return null;
			ICompilationUnit compilationUnit = (ICompilationUnit) root;

			IPath path = Path.fromOSString(compilationUnit.pathName());
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
			.findFilesForLocation(path);
			if (files.length == 1)
				return files[0];
			else
				return null;
		}
	}
	
	public static IFile getFile(String pathName) {
		IPath path = Path.fromOSString(pathName);
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
		.findFilesForLocation(path);
		if (files.length == 1)
			return files[0];
		else
			return null;
	}
	
	public static void openFile(IJastAddNode node) {
		if (node instanceof IJastAddJFindDeclarationNode) {
			IJastAddJFindDeclarationNode n = (IJastAddJFindDeclarationNode) node;
			int targetLine = n.selectionLine();
			int targetColumn = n.selectionColumn();
			int targetLength = n.selectionLength();
			ICompilationUnit cu = n.declarationCompilationUnit();
			openFile(cu, targetLine, targetColumn, targetLength);
		}
	}
	
	public static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));				
		char[] chars = new char[1024];
		int num = 0;
		while((num = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars, 0, num));	
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * Opens the file corresponding to the given compilation unit with a
	 * selection corresponding to the given line, column and length.
	 */
	protected static void openFile(ICompilationUnit unit, int line, int column,
			int length) {
		try {
			String pathName = unit.pathName();
			String relativeName = unit.relativeName();
			if (pathName == null || relativeName == null) 
				return;
			
			// Try to work as with resource
			if (pathName.equals(relativeName)) {
				IPath path = Path.fromOSString(pathName);
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
				for (ICompiler compiler : Activator.getRegisteredCompilers()) {
					if (files.length > 0 && compiler.canCompile(files[0])) {
						openEditor(new FileEditorInput(files[0]), line, column, length, FileInfoMap.buildFileInfo(files[0]));
						return;
					}
				}
			}
			
			IProject project = BuildUtil.getProject(unit);
			if (project == null)
				return;
			
			// Try to work as with class file 
			if (relativeName.endsWith(".class")) {
				openJavaSource(project, computeSourceName(unit), line, column, length);
				return;
			}
			
			// Try to work with source file paths 
			IStorage storage = null;
			
			IPath path = Path.fromOSString(pathName);
			File file = path.toFile();
			if (file.exists()) {
				storage = new LocalFileStorage(file);
			} else {
				IPath rootPath = path.removeTrailingSeparator();
				while (!rootPath.isEmpty() && !rootPath.toFile().exists()) {
					rootPath = rootPath.removeLastSegments(1).removeTrailingSeparator();
				}
				if (!rootPath.isEmpty() && rootPath.toFile().exists()) {
					IPath entryPath = path.removeFirstSegments(rootPath.segmentCount());
					try {
						storage = new ZipEntryStorage(new ZipFile(rootPath.toFile()), new ZipEntry(entryPath.toString()));
					} 
					catch(IOException e) {
						//logError(e, "Failed parsing ZIP entry");
					}
				}
			}
			
			if (storage != null)
				openEditor(new JastAddStorageEditorInput(project, storage), line, column, length, FileInfoMap.buildFileInfo(project, storage.getFullPath()));
		}
		catch(CoreException e) {
			//logCoreException(e);
		}
	}

	private static void openEditor(IEditorInput targetEditorInput,
			int line, int column, int length, FileInfo fileInfo)
			throws CoreException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		page.openEditor(targetEditorInput, JastAddJEditor.EDITOR_ID, true);
		IDocument targetDoc = FileInfoMap.fileInfoToDocument(fileInfo);
		if (targetDoc == null)
			return;
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
	
	protected static String computeSourceName(ICompilationUnit cu) {
		String sourceName = cu.relativeName();
		while (sourceName.endsWith(".class"))
			sourceName = sourceName.substring(0, sourceName.length() - 6);
		if (sourceName.contains("$")) {
			sourceName = sourceName.substring(0, sourceName.indexOf("$"));
		}
		if (!sourceName.endsWith(".java"))
			sourceName = sourceName + ".java";
		return sourceName;
	}

	
	protected static void openJavaSource(IProject project, String sourceName, int line, int column, int length) throws CoreException {
		JastAddJBuildConfiguration buildConfiguration = BuildUtil.getBuildConfiguration(project);
		if (buildConfiguration != null) {
			List<ISourceContainer> result= new ArrayList<ISourceContainer>();
			BuildUtil.populateSourceContainers(project, buildConfiguration, result);
			
			IEditorInput targetEditorInput = null;
			for(ISourceContainer sourceContainer : result) {
				try {
					Object[] elements = sourceContainer.findSourceElements(sourceName);
					if (elements.length == 1) {
						Object item = elements[0];
						if (item instanceof IFile) {
							IFile file = (IFile)item;
							targetEditorInput = null;
							for (ICompiler compiler : Activator.getRegisteredCompilers()) {
								if (compiler.canCompile(file)) {
									targetEditorInput = new FileEditorInput((IFile)item);
								}
							}
							if (targetEditorInput == null) {
								targetEditorInput = new JastAddStorageEditorInput(project, new LocalFileStorage(file.getRawLocation().toFile()));
							}
						}
						
						if (item instanceof LocalFileStorage)
							targetEditorInput = new JastAddStorageEditorInput(project, (IStorage)item);
						
						if (item instanceof ZipEntryStorage)
							targetEditorInput = new JastAddStorageEditorInput(project, (IStorage)item);												
						break;
					}
				}
				catch(CoreException e) {
					//logCoreException(e);
				}
			}
			
			if (targetEditorInput != null) {
				openEditor(targetEditorInput, line, column, length, FileInfoMap.buildFileInfo(targetEditorInput));
				return;
			}
		}
	}

	public static Object resolveResourceOrFile(IProject project, IPath path) {
		IResource resource;
		if (!path.isAbsolute())
			resource = project.findMember(path);
		else
			resource = project.getWorkspace().getRoot().findMember(path);

		if (resource != null)
			return resource;

		java.io.File file = path.toFile();
		if (file.exists())
			return file;

		return null;
	}

	
}
