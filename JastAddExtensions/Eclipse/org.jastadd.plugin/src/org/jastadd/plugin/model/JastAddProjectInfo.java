package org.jastadd.plugin.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;

public class JastAddProjectInfo {
	private JastAddModel model;
	private IProject project;
	
	protected JastAddProjectInfo(JastAddModel model, IProject project) {
		this.model = model;
		this.project = project;
	}
	
	public JastAddModel getJastAddModel()  {
		return model;
	}
	
	public IProject getProject()  {
		return project;
	}
	
	public void resourceChanged(IResourceChangeEvent event, IResourceDelta delta) {
	}
	
}
