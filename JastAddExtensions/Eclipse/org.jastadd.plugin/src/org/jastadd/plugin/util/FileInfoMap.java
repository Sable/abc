package org.jastadd.plugin.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class FileInfoMap {
	
	private static Map<IDocument, FileInfo> mapDocToFileInfo = new HashMap<IDocument, FileInfo>();
	private static Map<FileInfo, IDocument> mapFileInfoToDoc = new HashMap<FileInfo, IDocument>();
	
	
	public static FileInfo buildFileInfo(IEditorInput input) {
		if (input instanceof FileEditorInput)
			return buildFileInfo(((FileEditorInput)input).getFile());
		else if (input instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)input;
			return buildFileInfo(storageInput.getProject(), storageInput.getStorage().getFullPath());
		}
		return null;
	}
	
	public static FileInfo buildFileInfo(IFile file) {
		return new FileInfo(file.getProject(), file.getRawLocation());
	}
	
	public static FileInfo buildFileInfo(IProject project, IPath path) {
		return new FileInfo(project, path);
	}
	
	public static FileInfo documentToFileInfo(IDocument document) {
		return mapDocToFileInfo.get(document);
	}
	
	public static IDocument fileInfoToDocument(FileInfo fileInfo) {
		return mapFileInfoToDoc.get(fileInfo);
	}
	
	public static void linkFileInfoToDoc(FileInfo fileInfo, IDocument document) {
		mapDocToFileInfo.put(document, fileInfo);
		mapFileInfoToDoc.put(fileInfo, document);
	}
	
	public static void releaseFileInfo(FileInfo fileInfo) {
		IDocument document = mapFileInfoToDoc.get(fileInfo);
		mapFileInfoToDoc.remove(fileInfo);
		if(document != null)
			mapDocToFileInfo.remove(document);
	}
}