package org.jastadd.plugin.editor.folding;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddEditorFolder implements JastAddModelListener {
	private ProjectionAnnotationModel annotationModel;
	private Annotation[] oldAnnotations;
	private IEditorPart editorPart;
	
	public JastAddEditorFolder(ProjectionAnnotationModel annotationModel, 
			IEditorPart editorPart) {
		this.annotationModel = annotationModel;
		this.editorPart = editorPart;
	}
	
	public void updateFoldingStructure(List<Position> positions) {
		
		Annotation[] annotations = new Annotation[positions.size()];
		HashMap<Annotation,Object> newAnnotations = new HashMap<Annotation,Object>();
		
		int i = 0;
		for (Position pos : positions) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, pos);
			annotations[i++] = annotation;
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
				final IDocument document = model.fileInfoToDocument(model.buildFileInfo(file));

				// run update in the SWT UI thread
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						JastAddEditorConfiguration config = model.getEditorConfiguration();
						if (config != null) {
							List<Position> positions = config.getFoldingPositions(document);
							updateFoldingStructure(positions);
						}
					}
				});
			}
		}
	}
}
