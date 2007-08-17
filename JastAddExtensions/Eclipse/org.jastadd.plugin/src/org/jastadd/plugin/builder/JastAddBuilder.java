package org.jastadd.plugin.builder;

import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jastadd.plugin.JastAddModel;

public class JastAddBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.jastadd.plugin.jastaddBuilder";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
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
	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		JastAddModel.getInstance().fullBuild(getProject());
	}
		
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		JastAddModel.getInstance().fullBuild(getProject());
		//getProject().accept(new ResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new DeltaVisitor());
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
	
	private class DeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				JastAddModel.getInstance().fullBuild(resource.getProject());
				//checkFile(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				JastAddModel.getInstance().fullBuild(resource.getProject());
				//checkFile(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}
}
