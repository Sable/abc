package org.jastadd.plugin.jastaddj.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jastadd.plugin.jastaddj.nature.JastAddJNature;
import org.jastadd.plugin.wizards.JastAddNewProjectWizard;

public class JastAddJNewProjectWizard extends JastAddNewProjectWizard {

	@Override
	protected String createProjectPageDescription() {
		return "JastAddJ Project";
	}

	@Override
	protected String createProjectPageTitle() {
		return "Create New JastAddJ Project";
	}

	@Override
	protected String getNatureID() {
		return JastAddJNature.NATURE_ID;
	}
	
	protected void populateProject(IProject project, IProgressMonitor monitor) throws CoreException {
		
		// Create src directory
		IFolder srcFolder = project.getFolder(new Path("src"));
		if (!srcFolder.exists()) {
			srcFolder.create(false, true, monitor);
		} 
		
		// Create bin directory
		IFolder binFolder = project.getFolder(new Path("bin"));
		if (!binFolder.exists()) {
			binFolder.create(false, true, monitor);
		}
		
		// Add things to classpath
	}

}
