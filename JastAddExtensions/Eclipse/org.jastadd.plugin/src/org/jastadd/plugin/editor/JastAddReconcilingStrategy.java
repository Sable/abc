package org.jastadd.plugin.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.jastadd.plugin.model.JastAddModel;

public class JastAddReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

    private IDocument document;
    
	public void reconcile(IRegion partition) {
		initialReconcile();
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		initialReconcile();
	}

	public void setDocument(IDocument document) {
		this.document = document;
	}
	
	public void initialReconcile() {
		JastAddModel.getInstance().updateProjectModel(document);
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
	}
}
