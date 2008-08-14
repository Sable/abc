package org.jastadd.plugin.jastadd;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jastadd.plugin.jastaddj.wizards.JastAddJNewProjectWizard;
import org.osgi.framework.Bundle;

public class NewProjectWizard extends JastAddJNewProjectWizard {
	@Override protected String createProjectPageDescription() {
		return "JastAdd Project";
	}

	@Override protected String createProjectPageTitle() {
		return "Create New JastAdd Project";
	}

	@Override protected String getNatureID() {
		return Nature.NATURE_ID;
	}
	
	protected void populateProject(IProject project, IProgressMonitor monitor) throws CoreException {
		super.populateProject(project, monitor);
		
		// Create src/AST directory
		IFolder astFolder = project.getFolder(new Path("src/AST"));
		if (!astFolder.exists()) {
			astFolder.create(false, true, monitor);
		} 
		
		// Copy ASTNode.java, List.java and Opt.java to AST dir
		Bundle bundle = Platform.getBundle(Activator.JASTADD_PLUGIN_ID);
		if (bundle == null)
			return;
		InputStream in;
		try {
			// Copy ASTNode.java
			in = FileLocator.openStream(bundle, new Path("plugin-lib/ASTNode.java"), false);
			IFile file = project.getFile(new Path("src/AST/ASTNode.java"));
			file.create(in, true, monitor);
			in.close();
			// Copy List.java
			in = FileLocator.openStream(bundle, new Path("plugin-lib/List.java"), false);
			file = project.getFile(new Path("src/AST/List.java"));
			file.create(in, true, monitor);
			in.close();
			// Copy Opt.java
			in = FileLocator.openStream(bundle, new Path("plugin-lib/Opt.java"), false);
			file = project.getFile(new Path("src/AST/Opt.java"));
			file.create(in, true, monitor);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// TODO Add beaver-rt.jar to classpath
	}
}
