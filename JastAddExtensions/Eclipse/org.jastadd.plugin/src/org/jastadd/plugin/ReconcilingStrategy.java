package org.jastadd.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.jastadd.plugin.compiler.ICompiler;

/**
 * Reconciling strategy which updates the AST corresponding to a document in the AST registry.
 * 
 * @author emma
 *
 */
public class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	// Current document and corresponding file
	private IDocument document;
	private IFile file;

	/**
	 * Sets the file connected to the current document. Needed by the compiler calls 
	 * @param file
	 */
	public void setFile(IFile file) {
		this.file = file;
	}

	/*
	 * Calls the registered compilers
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion region) {		
		if (file != null) {
			for (ICompiler compiler : Activator.getRegisteredCompilers()) {
				if (compiler.canCompile(file)) {
					compiler.compile(document, null, region, file);
				}
			}
		}			
	}
	
	/*
	 * Calls registered compilers
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		if (file != null) {
			for (ICompiler compiler : Activator.getRegisteredCompilers()) {
				if (compiler.canCompile(file)) {
					compiler.compile(document, dirtyRegion, subRegion, file);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
		this.document = document;
	}

	/*
	 * Calls registered compilers
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		if (file != null) {
			for (ICompiler compiler : Activator.getRegisteredCompilers()) {
				if (compiler.canCompile(file)) {
					compiler.compile(document, null, null, file);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {	
	}
}