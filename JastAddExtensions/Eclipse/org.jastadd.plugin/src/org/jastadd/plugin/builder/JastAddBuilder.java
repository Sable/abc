package org.jastadd.plugin.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.jastadd.plugin.builder.JastAddBuilder";


	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		buildProject(getProject(), monitor);
	}
		
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		buildProject(getProject(), monitor);
		//getProject().accept(new ResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		DeltaVisitor deltaVisitor = new DeltaVisitor();
		delta.accept(deltaVisitor);
		if (deltaVisitor.buildRequired)		
			buildProject(getProject(), monitor);
		if(!deltaVisitor.changedFiles.isEmpty())
			updateProjectModel(getProject(), deltaVisitor.changedFiles);
	}

	
	/*
	private void run() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.jastadd.plugin.launchConfigurationType");
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "SampleConfig");
			
			IProject project = getProject();
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
			
			JastAddModel model = JastAddModel.getInstance();
			String[] mainClassList = model.getMainClassList();
			String mainClass = mainClassList.length > 0 ? mainClassList[0] : "";
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainClass);
			
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
			ILaunchConfiguration config = wc.doSave();	
			config.launch(ILaunchManager.DEBUG_MODE, null);
			//config.launch(ILaunchManager.RUN_MODE, null);
		} catch(CoreException e) {
		}
	}
	*/
	
/*	
	private class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			JastAddModel.getInstance().fullBuild(resource.getProject());
			//checkFile(resource);
			//return true to continue visiting children.
			return true;
		}
	}
	*/
	
	protected void updateProjectModel(IProject project, Collection<IFile> changedFiles) {
		for(JastAddModel m : JastAddModelProvider.getModels(project)) {
			m.updateProjectModel(changedFiles, project);
		}	
	}

	protected void buildProject(IProject project, IProgressMonitor monitor) {
		for(JastAddModel m : JastAddModelProvider.getModels(project)) {
			m.fullBuild(project, monitor);
		}	
	}

	
	private class DeltaVisitor implements IResourceDeltaVisitor {
		public boolean buildRequired = false;
		public Collection<IFile> changedFiles = new ArrayList<IFile>();
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				buildRequired = true;
				//checkFile(resource);
				if(delta.getResource() instanceof IFile)
					this.changedFiles.add((IFile)delta.getResource());
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				if(delta.getResource() instanceof IFile)
					this.changedFiles.add((IFile)delta.getResource());
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				buildRequired = true;
				if(delta.getResource() instanceof IFile)
					this.changedFiles.add((IFile)delta.getResource());
				//checkFile(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}
}
