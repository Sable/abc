package org.jastadd.plugin.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.jastadd.plugin.editor.JastAddStorageEditorInput;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddDocumentProvider extends FileDocumentProvider {
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(element instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)element).getFile();			
			JastAddModel model = JastAddModelProvider.getModel(file);
			if(model != null)
				model.linkFileInfoToDoc(model.buildFileInfo((IFileEditorInput)element), document);
		}
		else if (element instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput storageInput = (JastAddStorageEditorInput)element;
			JastAddModel model = storageInput.getModel(); 
			model.linkFileInfoToDoc(model.buildFileInfo(storageInput), document);
		}
		
		return document;
	}	
	
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof JastAddStorageEditorInput) {
			JastAddStorageEditorInput input= (JastAddStorageEditorInput) element;
			return new JastAddStorageAnnotationModel(input.getStorage().getFullPath());
		}		
		else
			return super.createAnnotationModel(element);
	}
	
}