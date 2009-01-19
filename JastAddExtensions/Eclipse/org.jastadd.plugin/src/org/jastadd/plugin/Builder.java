package org.jastadd.plugin;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jastadd.plugin.compiler.ICompiler;

/**
 * The JastAdd Core Builder
 * 
 * @author emma
 *
 */
public class Builder extends IncrementalProjectBuilder {

	// Plugin extension id
	public static final String BUILDER_ID = "org.jastadd.plugin.Builder";
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		// One method per build type in case this class will be extended
		switch (kind) {
		case IncrementalProjectBuilder.AUTO_BUILD :
			autoBuild(args, monitor);
			break;
		case IncrementalProjectBuilder.CLEAN_BUILD :
			cleanBuild(args, monitor);
			break;
		case IncrementalProjectBuilder.FULL_BUILD :
			fullBuild(args, monitor);
			break;
		case IncrementalProjectBuilder.INCREMENTAL_BUILD :
			incrementalBuild(args, monitor);
			break;
		}
		
		// We return null here but might change this 
		// when we start to use resource deltas
		return null;
	}

	
	/**
	 * Incremental build which tries to minimize build to only changed files
	 * @param args Build arguments
	 * @param monitor Progress monitor
	 */
	@SuppressWarnings("unchecked")
	protected void incrementalBuild(Map args, IProgressMonitor monitor) {
		
		IResourceDelta delta = getDelta(getProject());
		if (delta == null) {
			// No delta available, do a full build
			fullBuild(args, monitor);
		} else {
			DeltaVisitor deltaVisitor = new DeltaVisitor();
			try {
				// Collect deltas
				delta.accept(deltaVisitor);
				if (deltaVisitor.buildRequired) {
					// The delta might signal that a build is required if e.g., files have been added
					fullBuild(args, monitor);
				}
				
				// If something has been changed there will be change files
				if(!deltaVisitor.changedFiles.isEmpty()) {
					
					for (ICompiler compiler : Activator.getRegisteredCompilers()) {
						if (compiler.canCompile(getProject())) {
							compiler.compile(getProject(), deltaVisitor.changedFiles, monitor);
						}
					}
				}
				
			} catch (CoreException e) {
				String message = "Increamental build failed!"; 
				IStatus status = new Status(IStatus.ERROR, 
						Activator.PLUGIN_ID,
						IStatus.ERROR, message, e);
				Activator.getDefault().getLog().log(status);
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	protected void fullBuild(Map args, IProgressMonitor monitor) {
		for (ICompiler compiler : Activator.getRegisteredCompilers()) {
			if (compiler.canCompile(getProject())) {
				compiler.compile(getProject(), monitor);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void cleanBuild(Map args, IProgressMonitor monitor) {
		incrementalBuild(args, monitor);
	}

	@SuppressWarnings("unchecked")
	protected void autoBuild(Map args, IProgressMonitor monitor) {
		incrementalBuild(args, monitor);
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
			// return true to continue visiting children.
			return true;
		}
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

}
