package org.jastadd.plugin.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddDocumentProvider extends FileDocumentProvider {
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(element instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)element).getFile();			
			JastAddModel model = JastAddModelProvider.getModel(file);
			if(model != null)
				model.linkFileToDoc(file, document);
		}
		return document;
	}
}