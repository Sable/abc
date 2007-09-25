package org.jastadd.plugin.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;

import org.jastadd.plugin.AST.ASTNode;

public abstract class JastAddModel {
	
	private Map<IDocument, IFile> mapDocToFile = new HashMap<IDocument, IFile>();
	private Map<IFile, IDocument> mapFileToDoc = new HashMap<IFile, IDocument>();
	private Set<JastAddModelListener> modelListeners = new HashSet<JastAddModelListener>();
	
	protected JastAddEditorConfiguration editorConfig;
	protected final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	protected final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";
	
	
	
	public JastAddModel() {
		initModel();
	}
	
	public JastAddEditorConfiguration getEditorConfiguration() {
		return editorConfig;
	}

	public IFile documentToFile(IDocument document) {
		return mapDocToFile.get(document);
	}
	
	public IDocument fileToDocument(IFile file) {
		return mapFileToDoc.get(file);
	}
	
	public void linkFileToDoc(IFile file, IDocument document) {
		mapDocToFile.put(document, file);
		mapFileToDoc.put(file, document);
	}

	public void addListener(JastAddModelListener listener) {
		modelListeners.add(listener);
	}
	
	public void removeListener(JastAddModelListener listener) {
		modelListeners.remove(listener);
	}

	public void updateProjectModel(IDocument document) {
		IFile file = documentToFile(document);
		if(file == null) return;
		String fileName = file.getRawLocation().toOSString();
		updateProjectModel(document, fileName, file.getProject());
	}
	
	public synchronized void updateProjectModel(IDocument document, String fileName, IProject project) {
		updateModel(document, fileName, project);
		notifyModelListeners();
	}

	public ASTNode buildDocument(IDocument document, String fileName, IProject project) {
		try {
			updateProjectModel(document, fileName, project);
			return getTreeRoot(project, fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ASTNode getTreeRoot(IDocument document) {
		IFile file = documentToFile(document);
		if(file != null) {
			return getTreeRoot(file);
		}
		throw new UnsupportedOperationException("Can only get the current " +
			"compilation unit for a document that belongs to a JastAdd project");
	}
	
	public ASTNode getTreeRoot(IFile file) {
		try {
			return getTreeRoot(file.getProject(), file.getRawLocation().toOSString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized ASTNode getTreeRoot(IProject project, String filePath) {
		return getTreeRootNode(project, filePath);
	}
	
	
	public ASTNode findNodeInDocument(IDocument document, int offset) {
		return findNodeInDocument(documentToFile(document), offset);
	}
	
	public ASTNode findNodeInDocument(IDocument document, int line, int column) {
		return findNodeInDocument(documentToFile(document), line, column);
	}
	
	public ASTNode findNodeInDocument(IFile file, int offset) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = fileToDocument(file);
		return findNodeInDocument(project, fileName, document, offset);
	}
	
	public ASTNode findNodeInDocument(IFile file, int line, int column) {
		IProject project = file.getProject();
		String fileName = file.getRawLocation().toOSString();
		IDocument document = fileToDocument(file);
		return findNodeInDocument(project, fileName, document, line, column);
	}
	
	public ASTNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset) {
		try {
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			return findNodeInDocument(project, fileName, document, line, column);
		} catch (BadLocationException e) {
			return null;
		}
	}
	
	public ASTNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column) {
		ASTNode node = buildDocument(document, fileName, project);
		if(node != null)
			return findLocation(node, line + 1, column + 1);
		return null;
	}	
	
	
	public synchronized void fullBuild(IProject project) {
		completeBuild(project);
	}
	
	
	
	protected void initModel() {
		editorConfig = new JastAddEditorConfiguration(this);
	}

	protected String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));				
		char[] chars = new char[1024];
		while(reader.read(chars) > -1){
			sb.append(String.valueOf(chars));	
		}
		reader.close();
		return sb.toString();
	}

	protected void addParseErrorMarker(IFile file, String message, int lineNumber, int columnNumber, int severity) {
		try {
			IMarker marker = file.createMarker(PARSE_ERROR_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			DefaultLineTracker t = new DefaultLineTracker();
			t.set(readTextFile(file.getRawLocation().toOSString()));
			int offset = t.getLineOffset(lineNumber-1);
			offset += columnNumber - 1;
			marker.setAttribute(IMarker.CHAR_START, offset);
			marker.setAttribute(IMarker.CHAR_END, offset+1);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		} catch (IOException e) {
		} catch (BadLocationException e) {
		}
	}
	
	protected void deleteParseErrorMarkers(IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				file.deleteMarkers(PARSE_ERROR_MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} else if (res instanceof IFolder) {
			    IFolder folder = (IFolder) res;
			    deleteParseErrorMarkers(folder.members());
		   }
		}
	}
	
	protected void addErrorMarker(IFile file, String message, int lineNumber, int severity) throws CoreException {
		if (file != null) {
			IMarker marker = file.createMarker(ERROR_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		}
	}
	
	protected void deleteErrorMarkers(IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			if (res instanceof IFile && res.getName().endsWith(".java")) {
				IFile file = (IFile) res;
				file.deleteMarkers(ERROR_MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} else if (res instanceof IFolder) {
			    IFolder folder = (IFolder) res;
			    deleteErrorMarkers(folder.members());
		   }
		}
	}

	protected ASTNode findLocation(ASTNode node, int line, int column) {
		if(node == null) return node;
		int beginLine = node.getBeginLine();
		int beginColumn = node.getBeginColumn();
		int endLine = node.getEndLine();
		int endColumn = node.getEndColumn();
		
		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				if(node.getChild(i) != null) {
					ASTNode result = findLocation(node.getChild(i), line, column);
					if(result != null)
						return result;
				}
			}
			return null;
		}
		
		if((line >= beginLine && line <= endLine) &&
		   (line == beginLine && column >= beginColumn || line != beginLine) &&
		   (line == endLine && column <= endColumn || line != endLine)) {
			for(int i = 0; i < node.getNumChild(); i++) {
				if(node.getChild(i) != null) {
					ASTNode result = findLocation(node.getChild(i), line, column);
					if(result != null)
						return result;
				}
			}
			return node;
		}
		return null;
	}
	
		
	private void notifyModelListeners() {
		for(JastAddModelListener listener : modelListeners)
			listener.modelChangedEvent();
	}
	
	
	public abstract void openFile(ASTNode node);

	public abstract boolean isModelFor(IProject project);
	public abstract boolean isModelFor(IFile file);
	public abstract boolean isModelFor(IDocument document);
	public abstract boolean isModelFor(ASTNode node);	
	
	public abstract List<String> getFileExtensions();
	public abstract String[] getFilterExtensions();

	protected abstract void updateModel(IDocument document, String fileName, IProject project);
	protected abstract void completeBuild(IProject project);
	protected abstract ASTNode getTreeRootNode(IProject project, String filePath);


}