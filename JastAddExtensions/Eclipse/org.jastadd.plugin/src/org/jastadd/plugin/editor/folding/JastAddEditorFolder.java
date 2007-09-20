package org.jastadd.plugin.editor.folding;

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
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.resources.JastAddDocumentProvider;

public class JastAddEditorFolder implements JastAddModelListener {
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	private IEditorPart editorPart;
	private JastAddModel model;
	
	public JastAddEditorFolder(ProjectionAnnotationModel annotationModel, 
			IEditorPart editorPart, JastAddModel model) {
		this.annotationModel = annotationModel;
		this.editorPart = editorPart;
		this.model = model;
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
			final JastAddModel model = JastAddModelProvider.getModel(file);
			if (model != null) {
				final IDocument document = model.fileToDocument(file);

				// run update in the SWT UI thread
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ArrayList positions = model.getFoldingPositions(document);
						updateFoldingStructure(positions);
					}
				});
			}
		}
	}
}
