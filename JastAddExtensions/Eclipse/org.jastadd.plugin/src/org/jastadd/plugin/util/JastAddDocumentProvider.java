package org.jastadd.plugin.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.jastadd.plugin.ui.editor.BaseMarkerAnnotationModel;

public class JastAddDocumentProvider extends FileDocumentProvider {
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(element instanceof IFileEditorInput) {
			FileInfoMap.linkFileInfoToDoc(FileInfoMap.buildFileInfo((IFileEditorInput)element), document);
		}
		else if (element instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)element;
			FileInfoMap.linkFileInfoToDoc(FileInfoMap.buildFileInfo(storageInput), document);
		}
		
		return document;
	}	
	
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput input= (JastAddStorageEditorInput) element;
			return new BaseMarkerAnnotationModel(input.getStorage().getFullPath());
		}		
		else
			return super.createAnnotationModel(element);
	}
	
}