package org.jastadd.plugin.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.jastadd.plugin.JastAddDocumentProvider;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.JastAddModelListener;

public class JastAddEditorFolder implements JastAddModelListener {
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	private IEditorPart editorPart;
	
	public JastAddEditorFolder(ProjectionAnnotationModel annotationModel, IEditorPart editorPart) {
		this.annotationModel = annotationModel;
		this.editorPart = editorPart;
	}
	
	public void updateFoldingStructure(ArrayList positions) {
		
		Annotation[] annotations = new Annotation[positions.size()];
		//this will hold the new annotations along
		//with their corresponding positions
		HashMap<Annotation,Object> newAnnotations = new HashMap<Annotation,Object>();
		
		for(int i = 0; i < positions.size(); i++)
		{
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}
		
		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = annotations;
	}

	public void modelChangedEvent() {
		IEditorInput input = editorPart.getEditorInput();
		if(input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput)input;
			IFile file = fileInput.getFile();
			final IDocument document = JastAddDocumentProvider.fileToDocument(file);

			// run update in the SWT UI thread
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ArrayList positions = JastAddModel.getInstance().getFoldingPositions(document);
					updateFoldingStructure(positions);
				}
			});
		}
	}
}
