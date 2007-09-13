package org.jastadd.plugin.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.jastadd.plugin.JastAddModel;

public class JastAddReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private JastAddEditor editor;
    private IDocument document;
    protected ArrayList<Position> fPositions = new ArrayList<Position>();

	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	public void setDocument(IDocument document) {
		this.document = document;
	}
	
	public void setEditor(JastAddEditor editor) {
		this.editor = editor;
	}

	public void initialReconcile() {
		
		JastAddModel.getInstance().updateProjectModel(document);
		
		fPositions.clear();
		fPositions = JastAddModel.getInstance().getFoldingPositions(document);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(fPositions);
			}
		});
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		// TODO Auto-generated method stub		
	}
}
