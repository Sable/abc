package org.jastadd.plugin.compiler;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IError;
import org.jastadd.plugin.registry.ASTRegistry;

/**
 * Abstract compiler implementation of  the ICompiler interface required by the
 * org.jastadd.plugin.compiler extension point. Adds some useful protected methods
 * which can be used to, e.g., add error markers.
 * 
 * @author emma
 *
 */
public abstract class AbstractCompiler implements ICompiler {

	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.ICompiler#canCompile(org.eclipse.core.resources.IFile)
	 */
	public boolean canCompile(IFile file) {
		if(file == null)
			return false;
		for (String str : acceptedFileExtensions()) {
			if (file.getFileExtension() != null && file.getFileExtension().equals(str)) {
				return canCompile(file.getProject());
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.ICompiler#canCompile(org.eclipse.core.resources.IProject)
	 */
	public boolean canCompile(IProject project) {
		try {
			if (project != null && project.isOpen()
					&& project.isNatureEnabled(acceptedNatureID())) {
				return true;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.ICompiler#compile(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void compile(IProject project, IProgressMonitor monitor) {
		IASTNode root = compileToProjectAST(project, monitor);
		ASTRegistry reg = Activator.getASTRegistry();
		if (reg != null && root != null && root.isProjectAST()) {
			reg.updateProjectAST(root, project);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.ICompiler#compile(org.eclipse.core.resources.IProject, java.util.Collection, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void compile(IProject project, Collection<IFile> changedFiles, IProgressMonitor monitor) {
		ASTRegistry reg = Activator.getASTRegistry();
		for (IFile file : changedFiles) {
			IASTNode node = compileToAST(file);
			if (reg != null && node != null && node.hasLookupKey()) {
				reg.updateAST(node, node.lookupKey(), file);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jastadd.plugin.compiler.ICompiler#compile(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion, org.eclipse.core.resources.IFile)
	 */
	public void compile(IDocument document, DirtyRegion dirtyRegion, IRegion region, IFile file) {
		IASTNode node = compileToAST(document, dirtyRegion, region, file);
		ASTRegistry reg = Activator.getASTRegistry();
		if (reg != null && node != null && node.hasLookupKey()) {
			reg.updateAST(node, node.lookupKey(), file);
		}
		displaySemanticErrors(node, file);
	}
	
	/**
	 * Called after a document has been compiled and added to the AST registry. 
	 * Override to collect and display semantic errors
	 * @param node The new root node
	 * @param file The corresponding file
	 */
	protected void displaySemanticErrors(IASTNode node, IFile file) {
	}
	
	/**
	 * Updates error markers in a given resource based on a collection of errors
	 * @param resource The resource to add markers to
	 * @param errorList The list of errors
	 * @param markerID The id of the markers
	 */
	protected void updateErrorMarkers(IResource resource, Collection<IError> errorList, String markerID) {
		if (resource == null)
			return;
		deleteErrorMarkers(resource, markerID);
		addErrorMarkers(resource, errorList, markerID);	
	}
	
	/**
	 * Adds error markers to a resource
	 * @param resource The resource to add markers to
	 * @param errorList The list of errors
	 * @param markerID The marker ID to use
	 */
	protected void addErrorMarkers(IResource resource, Collection<IError> errorList, String markerID) {
		for (IError error : errorList) {
			addErrorMarker(resource, error, markerID);				
		}
	}
	
	/**
	 * Adds an error marker to a resource
	 * @param resource The resource to add the marker to
	 * @param error The error
	 * @param markerID The marker ID to use
	 */
	protected void addErrorMarker(IResource resource, IError error, String markerID) {
		try {
			IMarker marker = resource.createMarker(markerID);

			String message = error.getMessage();
			int severity = error.getSeverity();
			int line = error.getLine();
			int startOffset = error.getStartOffset();
			int endOffset = error.getEndOffset();

			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (line < 0)
				line = 1;
			marker.setAttribute(IMarker.LINE_NUMBER, line);
			if (startOffset > 0 && endOffset > 0 && endOffset > startOffset) {
				marker.setAttribute(IMarker.CHAR_START, startOffset);
				marker.setAttribute(IMarker.CHAR_END, endOffset);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a marker to show that compilation failed
	 * @param resource The resource to put the marker on
	 * @param message The message explaining the failure
	 */
	protected void addCompilationFailedMarker(IResource resource, String message) {
		try {
			IMarker marker = resource.createMarker(ERROR_MARKER_ID);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.LINE_NUMBER, -1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes all error markers with the given ID occuring in the given IResource
	 * @param resource The resource to delete error markers in
	 * @param markerID The id of the markers to delete
	 */
	protected void deleteErrorMarkers(IResource resource, String markerID) {
		try {
			resource.deleteMarkers(markerID, false, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a collection of file extensions accepted by this compiler
	 * @return Collection of file extensions
	 */
	protected abstract Collection<String> acceptedFileExtensions();
	
	/**
	 * Returns a string with the nature id accepted by this compiler
	 * @return String with the accepted nature id
	 */
	protected abstract String acceptedNatureID();
	
	/** 
	 * Compiles an IProject and returns the root of the complete AST for a project.
	 * Should always return something from which errors can be collected 
	 * @param project The project to compile
	 * @param monitor Monitor displaying progress
	 * @return An IASTNode to add to the registry 
	 */
	protected abstract IASTNode compileToProjectAST(IProject project, IProgressMonitor monitor);
	
	/**
	 * Compiles a document to an IASTNode
	 * @param document The document to compile
	 * @param dirtyRegion Dirty region in the document
	 * @param region Changed region in the document
	 * @param file The file containing the source of the document
	 * @return An IASTNode, should never be null
	 */
	protected abstract IASTNode compileToAST(IDocument document, DirtyRegion dirtyRegion, IRegion region, IFile file);
	
	/**
	 * Compiles a file to an IASTNode
	 * @param file The file to compiler
	 * @return An IASTNode, should never be null
	 */
	protected abstract IASTNode compileToAST(IFile file);
}
