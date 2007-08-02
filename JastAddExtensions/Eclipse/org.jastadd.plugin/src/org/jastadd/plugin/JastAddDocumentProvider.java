package org.jastadd.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class JastAddDocumentProvider extends FileDocumentProvider {
	static Map<IDocument, IFile> mapDocToFile = new HashMap<IDocument, IFile>();
	static Map<IFile, IDocument> mapFileToDoc = new HashMap<IFile, IDocument>();
	
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(element instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)element).getFile();
			mapDocToFile.put(document, file);
			mapFileToDoc.put(file, document);
		}
		return document;
	}
	
	public static IFile documentToFile(IDocument document) {
		return mapDocToFile.get(document);
	}
	
	public static IDocument fileToDocument(IFile file) {
		return mapFileToDoc.get(file);
	}
}