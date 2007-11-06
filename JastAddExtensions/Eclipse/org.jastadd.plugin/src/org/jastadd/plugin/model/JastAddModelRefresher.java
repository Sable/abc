package org.jastadd.plugin.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

public class JastAddModelRefresher implements IResourceChangeListener {
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getDelta() == null) {
			if (event.getSource() instanceof IProject) {
				IProject project = (IProject)event.getSource();
				updateProject(event, project);
			}
			else if (event.getSource() instanceof IWorkspace) {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (IProject project : projects) {
					updateProject(event, project);
				}				
			}
			else if (event.getResource() instanceof IProject) {
				IProject project = (IProject)event.getResource();
				updateProject(event, project);
			}	
		}
		else  {
			IResourceDelta[] children = event.getDelta().getAffectedChildren();
			for(IResourceDelta child : children) {
				IProject project = (IProject)child.getResource();
				updateProject(event, project);
			}
		}
	}

	private void updateProject(IResourceChangeEvent event, IProject project) {
		List<JastAddModel> models = JastAddModelProvider
				.getModels(project);
		for (JastAddModel model : models) {
			if (model.isModelFor(project)) {
				if(event.getType() == IResourceChangeEvent.PRE_CLOSE && event.getResource() == project) {
					model.discardTree(project);
				}
				model.resourceChanged(project, event, event.getDelta() != null ? event.getDelta().findMember(new Path(project.getName())) : null);
			}
		}
	}
}