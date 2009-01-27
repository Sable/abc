package org.jastadd.plugin.compiler;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;

/**
 * Compiler interface required by the org.jastadd.plugin.compiler extension point:
 * 
 * <extension id="..."
 *        name="..."
 *        point="org.jastadd.plugin.compiler">
 *    <compiler class="Class implementing this interface">
 *    </compiler>
 * </extension>
 * 
 * Contains two methods. The first is called by the builder when
 * a project is rebuilt and the second is called by the reconciling
 * strategy when an open file has been changed.
 * 
 * @author emma
 *
 */
public interface ICompiler {

	/*
	 * Previously used error marker:
	 * "org.eclipse.ui.workbench.texteditor.error";
	 * Previously used warning marker:
	 * "org.eclipse.ui.workbench.texteditor.warning";
	 */
	
	public static final String EXTENSION_POINT_ID = "org.jastadd.plugin.compilers";
	public static final String EXTENSION_ATTR_CLASS = "class";
	
	public static final String ERROR_MARKER_ID = "org.jastadd.plugin.marker.ErrorMarker";
	public static final String PARSE_ERROR_MARKER_ID = "org.jastadd.plugin.marker.ParseErrorMarker";
	public static final String WARNING_MARKER_ID = "org.jastadd.plugin.marker.WarningMarker";
	
	/**
	 * Compiles all files in a project and update resource markers
	 * @param project The project to compile
	 * @param monitor Progress monitor which might signal that compilation should be canceled
	 */
	public void compile(IProject project, IProgressMonitor monitor);

	/**
	 * Compiles a set of changed files in a project and updates resource markers 
	 * @param project The project in which the files belong
	 * @param changedFiles A collection of changed files
	 * @param monitor Progress monitor which might signal that compilation should be canceled
	 */
	public void compile(IProject project, Collection<IFile> changedFiles, IProgressMonitor monitor);
	
	/**
	 * Compilers a single document
	 * @param document The document to compile
	 * @param dirtyRegion The changes region
	 * @param region The changed region
	 * @param file The corresponding file, used for error annotations
	 */
	public void compile(IDocument document, DirtyRegion dirtyRegion, IRegion region, IFile file);

	/**
	 * Checks if this compiler can compile the given project.
	 * @param project The project to check
	 * @return true if the project can be compiled
	 */
	public boolean canCompile(IProject project);

	/**
	 * Checks if this compiler can compile the given file.
	 * @param file The file to check
	 * @return true if this file can be compilers
	 */
	public boolean canCompile(IFile file);
}
