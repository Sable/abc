package org.jastadd.plugin.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.model.repair.RecoveryLexer;
import org.jastadd.plugin.util.JastAddPair;

public abstract class JastAddModel {

	public static class FileInfo extends JastAddPair<IProject, IPath> {
		public FileInfo(IProject project, IPath path) {
			super(project, path);
		}
		
		public IProject getProject() {
			return first;
		}
		
		public IPath getPath() {
			return second;
		}
	}
	
	private Map<IDocument, FileInfo> mapDocToFileInfo = new HashMap<IDocument, FileInfo>();
	private Map<FileInfo, IDocument> mapFileInfoToDoc = new HashMap<FileInfo, IDocument>();
	private Set<JastAddModelListener> modelListeners = new HashSet<JastAddModelListener>();
	
	private Map<String,RuleBasedScanner> mapFileTypeToScanner = new HashMap<String,RuleBasedScanner>();
	protected ArrayList<String> fileTypeList = new ArrayList<String>();
	
	protected JastAddEditorConfiguration editorConfig;
	protected final String ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ErrorMarker";
	protected final String PARSE_ERROR_MARKER_TYPE = "org.jastadd.plugin.marker.ParseErrorMarker";
	
	public JastAddModel() {
		initModel();
	}
	
	public JastAddEditorConfiguration getEditorConfiguration() {
		return editorConfig;
	}
	
	protected void registerScanner(RuleBasedScanner scanner, String fileType) {
		mapFileTypeToScanner.put(fileType, scanner);
	}
	
	protected void registerFileType(String fileType) {
		fileTypeList.add(fileType);
	}
	
	public RuleBasedScanner getScanner(IFile file) {
		return mapFileTypeToScanner.get(file.getFileExtension());
	}
	
	public boolean hasErrors(IProject project) {
		try {
			IMarker[] errorMarkers = project.findMarkers(ERROR_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			IMarker[] parseErrorMarkers = project.findMarkers(PARSE_ERROR_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			return errorMarkers.length > 0 || errorMarkers.length > 0; 
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	public FileInfo buildFileInfo(IEditorInput input) {
		if (input instanceof FileEditorInput)
			return buildFileInfo(((FileEditorInput)input).getFile());
		else if (input instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
			return buildFileInfo(storageInput.getProject(), storageInput.getStorage().getFullPath());
		}
		return null;
	}
	
	public FileInfo buildFileInfo(IFile file) {
		return new FileInfo(file.getProject(), file.getRawLocation());
	}
	
	protected static FileInfo buildFileInfo(IProject project, IPath path) {
		return new FileInfo(project, path);
	}
	
	public FileInfo documentToFileInfo(IDocument document) {
		return mapDocToFileInfo.get(document);
	}
	
	public IDocument fileInfoToDocument(FileInfo fileInfo) {
		return mapFileInfoToDoc.get(fileInfo);
	}
	
	public void linkFileInfoToDoc(FileInfo fileInfo, IDocument document) {
		mapDocToFileInfo.put(document, fileInfo);
		mapFileInfoToDoc.put(fileInfo, document);
	}
	
	public void releaseFileInfo(FileInfo fileInfo) {
		IDocument document = mapFileInfoToDoc.get(fileInfo);
		mapFileInfoToDoc.remove(fileInfo);
		if(document != null)
			mapDocToFileInfo.remove(document);
	}

	public void addListener(JastAddModelListener listener) {
		modelListeners.add(listener);
	}
	
	public void removeListener(JastAddModelListener listener) {
		modelListeners.remove(listener);
	}

	public void updateProjectModel(Collection<IFile> changedFiles, IProject project) {
		synchronized(getASTRootForLock(project)) {
			updateModel(changedFiles, project);
		}
		notifyModelListeners();
	}

	public void updateProjectModel(IDocument document) {
		FileInfo fileInfo = documentToFileInfo(document);
		if(fileInfo == null) return;
		
		synchronized (getASTRootForLock(fileInfo.getProject())) {
			updateProjectModel(document, fileInfo.getPath().toOSString(), fileInfo.getProject());
		}
	}
	
	public void updateProjectModel(IDocument document, String fileName, IProject project) {
		synchronized(getASTRootForLock(project)) {
			updateModel(document, fileName, project);
		}
		notifyModelListeners();
	}

	public IJastAddNode buildDocument(IDocument document, String fileName, IProject project) {
		try {
			updateProjectModel(document, fileName, project);
			return getTreeRoot(project, fileName);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public IJastAddNode getTreeRoot(IDocument document) {
		FileInfo fileInfo = documentToFileInfo(document);
		if(fileInfo != null) {
			return getTreeRoot(fileInfo);
		}
		return null;
		/*
		throw new UnsupportedOperationException("Can only get the current " +
			"compilation unit for a document that belongs to a JastAdd project");
		*/
	}
	
	public IJastAddNode getTreeRoot(FileInfo fileInfo) {
		try {
			synchronized (getASTRootForLock(fileInfo.getProject())) {
				return getTreeRoot(fileInfo.getProject(), fileInfo.getPath().toOSString());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public abstract IFile getFile(IJastAddNode node);

	public IJastAddNode getTreeRoot(IProject project, String filePath) {
		synchronized(getASTRootForLock(project)) {
			return getTreeRootNode(project, filePath);
		}
	}
	
	
	public IJastAddNode findNodeInDocument(IDocument document, int offset) {
		return findNodeInDocument(documentToFileInfo(document), offset);
	}
	
	public IJastAddNode findNodeInDocument(IDocument document, int line, int column) {
		FileInfo info = documentToFileInfo(document);
		if (info == null) 
			return null;
		return findNodeInDocument(info, line, column);
	}
	
	public IJastAddNode findNodeInDocument(FileInfo fileInfo, int offset) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = fileInfoToDocument(fileInfo);
		if (document == null)
			return null;
		return findNodeInDocument(project, fileName, document, offset);
	}
	
	public IJastAddNode findNodeInDocument(FileInfo fileInfo, int line, int column) {
		IProject project = fileInfo.getProject();
		String fileName = fileInfo.getPath().toOSString();
		IDocument document = fileInfoToDocument(fileInfo);
		return findNodeInDocument(project, fileName, document, line, column);
	}
	
	public IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int offset) {
		try {
			int line = document.getLineOfOffset(offset);
			int column = offset - document.getLineOffset(line);
			return findNodeInDocument(project, fileName, document, line, column);
		} catch (BadLocationException e) {
			return null;
		}
	}
	
	public IJastAddNode findNodeInDocument(IProject project, String fileName, IDocument document, int line, int column) {
		IJastAddNode node = getTreeRoot(project, fileName);
		if(node == null) {
		  node = buildDocument(document, fileName, project);
		}
		if(node != null)
			return findLocation(node, line + 1, column + 1);
		return null;
	}	
	
	
	public void fullBuild(IProject project, IProgressMonitor monitor) {
		completeBuild(project, monitor);
	}

	protected abstract void initModel();
	
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

	protected void addParseErrorMarker(IFile file, String message, int line, int startOffset, 
			int endOffset, int severity) throws CoreException {		
		if (file == null)
			return;
		IMarker marker = file.createMarker(PARSE_ERROR_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		if (line < 0)
			line = 1;
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		if (startOffset > 0 && endOffset > 0 && endOffset > startOffset) {
			marker.setAttribute(IMarker.CHAR_START, startOffset);
			marker.setAttribute(IMarker.CHAR_END, endOffset);
		} 
	}
	
	protected void addErrorMarker(IResource resource, String message, int line, 
			int severity) throws CoreException {
		if (resource == null)
			return;
		IMarker marker = resource.createMarker(ERROR_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.LINE_NUMBER, line);		
	}
	
	protected void addErrorMarker(IFile file, String message, int line, int startOffset, 
			int endOffset, int severity) throws CoreException {
		if (file == null)
			return;
		IMarker marker = file.createMarker(ERROR_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		if (line < 0)
			line = 1;
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		if (startOffset > 0 && endOffset > 0 && endOffset > startOffset) {
			marker.setAttribute(IMarker.CHAR_START, startOffset);
			marker.setAttribute(IMarker.CHAR_END, endOffset);
		}
	}
	
	protected void deleteErrorMarkers(String markerType, IResource resource) throws CoreException {
		deleteErrorMarkers(markerType, new IResource[] { resource } );
	}
	
	protected void deleteErrorMarkers(String markerType, IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource res = resources[i];
			//if (res instanceof FileInfo && isModelFor((FileInfo)res)) {
			if (res instanceof IFile && isModelFor((IFile)res)) {
				IFile file = (IFile) res;
				file.deleteMarkers(markerType, false, IResource.DEPTH_ONE);
			} else if (res instanceof IFolder) {
			    IFolder folder = (IFolder) res;
			    folder.deleteMarkers(markerType, false, IResource.DEPTH_INFINITE);
		   } else if (res instanceof IProject) {
			    IProject project = (IProject) res;
			    project.deleteMarkers(markerType, false, IResource.DEPTH_INFINITE);
		   }
		}
	}

	protected IJastAddNode findLocation(IJastAddNode node, int line, int column) {
		if(node == null) return null;
		int beginLine = node.getBeginLine();
		int beginColumn = node.getBeginColumn();
		int endLine = node.getEndLine();
		int endColumn = node.getEndColumn();

		if(beginLine == 0 && beginColumn == 0 && endLine == 0 && endColumn == 0) {
			for(int i = 0; i < node.getNumChild(); i++) {
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column);
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
				IJastAddNode child = node.getChild(i);
				if(child != null) {
					IJastAddNode result = findLocation(child, line, column);
					if(result != null)
						return result;
				}
			}
			return node;
		}

		return null;
	}
	
		
	protected void notifyModelListeners() {
		// NB! This thread should not be synchronized on this!
		for(JastAddModelListener listener : modelListeners.toArray(new JastAddModelListener[0]))
			listener.modelChangedEvent();
	}
	
	public void resourceChanged(IProject project, IResourceChangeEvent event, IResourceDelta delta) {}
	
	public abstract void openFile(IJastAddNode node);
	
	public abstract String getEditorID();
	public abstract String getNatureID();
	
	public abstract boolean isModelFor(IProject project);
	public abstract boolean isModelFor(IFile file);
	public abstract boolean isModelFor(FileInfo fileInfo);
	public abstract boolean isModelFor(IDocument document);
	public abstract boolean isModelFor(IJastAddNode node);	
	
	public abstract List<String> getFileExtensions();
	public abstract String[] getFilterExtensions();
	
	public abstract Object getASTRootForLock(IProject project);
	public abstract void checkForErrors(IProject project, IProgressMonitor monitor);
	
	private boolean commandsPopulated = false;
	
	// Why synchronized ?
	public synchronized void registerCommands() {
		if (commandsPopulated) return;
		try {
			getEditorConfiguration().populateCommands();
			commandsPopulated = true;
		}
		catch(Exception e) {
			logError(e, "Failed registering commands");
			return;
		}
	}
	
	public abstract void registerStopHandler(Runnable stopHandler);

	protected abstract void updateModel(IDocument document, String fileName, IProject project);
	protected abstract void updateModel(Collection<IFile> changedFiles, IProject project);
	protected abstract void completeBuild(IProject project, IProgressMonitor monitor);
	protected abstract IJastAddNode getTreeRootNode(IProject project, String filePath);
	protected abstract void discardTree(IProject project);
	
	public abstract RecoveryLexer getRecoveryLexer();

	public abstract void logStatus(IStatus status);
	
	public void logError(Throwable t, String message) {
		logStatus(makeErrorStatus(t, message));
	}
	
	public void logCoreException(CoreException e) {
		logStatus(e.getStatus());
	}
	
	public CoreException makeCoreException(Throwable e, String message) {
		return new CoreException(makeErrorStatus(e, message));
	}

	public abstract IStatus makeErrorStatus(Throwable e, String message);
	
	public void displayError(Throwable t, Shell shell, String title, String message) {
		StringWriter msg= new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
			msg.write(t.getMessage());
		}
		MessageDialog.openError(shell, title, msg.toString());			
	}

	protected String[] filterNames = {".project"};
	
	public boolean filterInExplorer(String resourceName) {
		for (int i = 0; i < filterNames.length; i++) {
			if (resourceName.equals(filterNames[i])) {
				return true;
			}
		}
		return false;
	}
}
