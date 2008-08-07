package org.jastadd.plugin.jastaddj.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddJLaunchDelegate extends JavaLaunchDelegate {
	private IProject project;
	private JastAddJModel model;
	private JastAddJBuildConfiguration buildConfiguration;
	
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) return;
		
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		model = JastAddModelProvider.getModel(project, JastAddJModel.class);	
		
		buildConfiguration = model.readBuildConfiguration(project);

		if (model != null && project != null && model.hasErrors(project)) {
			// Show info 
			System.out.println("Errors in project, cannot launch");
			return;
		}
		
		super.launch(configuration, mode, launch, monitor);
	}
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		if (!saveBeforeLaunch(configuration, mode, monitor)) {
			return false;
		}
		
		if (mode.equals(ILaunchManager.RUN_MODE) && configuration.supportsMode(ILaunchManager.DEBUG_MODE)) {
			IBreakpoint[] breakpoints= getBreakpoints(configuration);
            if (breakpoints == null) {
                return true;
            }
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i].isEnabled()) {
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
					if (prompter != null) {
						boolean launchInDebugModeInstead = ((Boolean)prompter.handleStatus(switchToDebugPromptStatus, configuration)).booleanValue();
						if (launchInDebugModeInstead) { 
							return false; //kill this launch
						} 
					}
					// if no user prompt, or user says to continue (no need to check other breakpoints)
					return true;
				}
			}
		}
		ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
		
		//IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		//IProject project = getProject();
		//wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "Test");
		
		/*
		JastAddModel model = JastAddModel.getInstance();
		String[] mainClassList = model.getMainClassList();
		String mainClass = mainClassList.length > 0 ? mainClassList[0] : "";
		*/
		//wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "MainMain");
		//wc.setAttribute(attributeName, value)
		// no enabled breakpoints... continue launch
		return true;
	}

	public IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		return model.getVMInstall(project, buildConfiguration);
	}

	public String[][] getBootpathExt(ILaunchConfiguration configuration) throws CoreException {
		String[][] bootpathInfo = new String[3][];
		return bootpathInfo;
	}
	
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {	
		String path = project.getLocation().toOSString();
		String[] defaultClassPath = new String[] { path };
		
		List<String> classPath = new ArrayList<String>();
		model.populateClassPath(project, buildConfiguration, classPath);
		return classPath.toArray(new String[0]);
	}

	protected void setDefaultSourceLocator(ILaunch launch,
			ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			ISourceLookupDirector sourceLocator = new JastAddJSourceLookupDirector(project, model, buildConfiguration);
			sourceLocator
					.setSourcePathComputer(getLaunchManager()
							.getSourcePathComputer(
									"org.jastadd.plugin.launcher.SourceLocator")); //$NON-NLS-1$
			sourceLocator.initializeDefaults(configuration);
			launch.setSourceLocator(sourceLocator);
		}
	}
}
