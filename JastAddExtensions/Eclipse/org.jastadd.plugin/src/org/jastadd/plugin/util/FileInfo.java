package org.jastadd.plugin.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class FileInfo extends JastAddPair<IProject, IPath> {
	public FileInfo(IProject project, IPath path) {
		super(project, path);
	}
	
	public IProject getProject() {
		return first;
	}
	
	public IPath getPath() {
		return second;
	}

}
