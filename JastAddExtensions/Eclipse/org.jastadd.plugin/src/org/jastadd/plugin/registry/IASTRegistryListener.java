package org.jastadd.plugin.registry;

import org.eclipse.core.resources.IProject;

/**
 * Interface listening to changes in the org.jastadd.plugin.ASTRegistry
 * 
 * @author emma
 *
 */
public interface IASTRegistryListener {
	
	/**
	 * The root AST connected to the given project has been updated  
	 * @param project The project related to the root AST
	 */
	public void projectASTChanged(IProject project);
	
	/**
	 * A child AST with the given key in the root AST related to the given 
	 * project has been updated.
	 * @param project The project related to the root AST
	 * @param key The lookup key of the child AST which has been updated
	 */
	public void childASTChanged(IProject project, String key);
}
