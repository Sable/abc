package org.jastadd.plugin.jastaddj.launcher;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.editor.debug.JastAddJBreakpoint;
import org.jastadd.plugin.jastaddj.util.BuildUtil;

public class JastAddJLaunchDelegate extends LaunchConfigurationDelegate {
	
	protected IProject[] fOrderedProjects;
	private IProject project;
	//private JastAddJModel model;
	private JastAddJBuildConfiguration buildConfiguration;
	
	
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.subTask("Verifying launch attributes..."); 
							
			String mainTypeName = verifyMainTypeName(configuration);		
			IVMRunner runner = getVMRunner(configuration, mode);
	
			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null) {
				workingDirName = workingDir.getAbsolutePath();
			}
			
			// Environment variables
			String[] envp= getEnvironment(configuration);
			
			// Program & VM arguments
			String pgmArgs = getProgramArguments(configuration);
			String vmArgs = getVMArguments(configuration);
			ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
			
			// VM-specific attributes
			Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
			
			// Classpath
			String[] classpath = getClasspath(configuration);
			
			// Create VM config
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
			runConfig.setEnvironment(envp);
			runConfig.setVMArguments(execArgs.getVMArgumentsArray());
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);
	
			// Bootpath
			runConfig.setBootClassPath(getBootpath(configuration));
			
			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}		
			
			// stop in main
			//prepareStopInMain(configuration);
			
			// done the verification phase
			monitor.worked(1);
			
			monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2); 
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);		
			
			// Launch the configuration - 1 unit of work
			runner.run(runConfig, launch, monitor);
			
			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}	
		}
		finally {
			monitor.done();
		}
	}

	private String[] getBootpath(ILaunchConfiguration configuration) throws CoreException {
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] main = paths[1];
		String[] app = paths[2];
		if (pre == null && main == null && app == null) {
			// default
			return null;
		}
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List bootEntries = new ArrayList(entries.length);
		boolean empty = true;
		boolean allStandard = true;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					empty = false;
					bootEntries.add(location);
					allStandard = allStandard
							&& entries[i].getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES;
				}
			}
		}
		if (empty) {
			return new String[0];
		} else if (allStandard) {
			return null;
		} else {
			return (String[]) bootEntries
					.toArray(new String[bootEntries.size()]);
		}
	}

	private Map getVMSpecificAttributesMap(ILaunchConfiguration configuration) throws CoreException {		
		Map map = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
				(Map) null);
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] boot = paths[1];
		String[] app = paths[2];
		if (pre != null || app != null || boot != null) {
			if (map == null) {
				map = new HashMap(3);
			}
			if (pre != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND, pre);
			}
			if (app != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND, app);
			}
			if (boot != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, boot);
			}
		}
		return map;
	}

	private String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
	}

	private String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
	}

	private File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException { 
		String pathName = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null); 
		if (pathName == null) {
			File dir = project.getLocation().toFile();
			if (dir != null) {
				if (!dir.isDirectory()) {
					return null; 
				}
				return dir;
			}
		} else {
			IPath path = new Path(pathName);
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory()) {
					return dir;
				}
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				return null;
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot()
						.findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				return null;			
			}
		}
		return null;
	}

	private String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
	}
		
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) 
			return false;

		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null)
			return false;
		buildConfiguration = BuildUtil.readBuildConfiguration(project);
		if (buildConfiguration == null)
			return false;
		
		fOrderedProjects = null;
		fOrderedProjects = computeReferencedBuildOrder(new IProject[]{project});
		
		return super.preLaunchCheck(configuration, mode, monitor);
	}


	protected IProject[] getBuildOrder(ILaunchConfiguration configuration,
			String mode) throws CoreException {
		return fOrderedProjects;
	}
	
	protected IProject[] getProjectsForProblemSearch(
			ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return fOrderedProjects;
	}
	
	protected IBreakpoint[] getBreakpoints(ILaunchConfiguration configuration) {
        IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        if (!breakpointManager.isEnabled()) {
            // no need to check breakpoints individually.
            return null;
        }
        //return breakpointManager.getBreakpoints(JastAddJBreakpoint.MARKER_ID);
        return breakpointManager.getBreakpoints(JastAddJBreakpoint.JAVA_LINE_BREAKPOINT);
    }

	
	// Boot paths

	public String[][] getBootpathExt(ILaunchConfiguration configuration) throws CoreException {
		String[][] bootpathInfo = new String[3][];
		return bootpathInfo;
	}
	
	// Class paths
	
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {	
		String path = project.getLocation().toOSString();
		String[] defaultClassPath = new String[] { path };
		
		List<String> classPath = new ArrayList<String>();
		BuildUtil.populateClassPath(project, buildConfiguration, classPath);
		return classPath.toArray(new String[0]);
	}

	// Source Locator
	
	protected void setDefaultSourceLocator(ILaunch launch,
			ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			ISourceLookupDirector sourceLocator = new JastAddJSourceLookupDirector(project/*, model*/, buildConfiguration);
			sourceLocator
					.setSourcePathComputer(getLaunchManager()
							.getSourcePathComputer(
									"org.jastadd.plugin.launcher.SourceLocator")); //$NON-NLS-1$
			sourceLocator.initializeDefaults(configuration);
			launch.setSourceLocator(sourceLocator);
		}
	}
	
	// VM
	
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	public IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		return BuildUtil.getVMInstall(project, buildConfiguration);
	}
/*
	public IVMInstall getVMInstall(ILaunchConfiguration configuration)
		throws CoreException {
		return JavaRuntime.computeVMInstall(configuration);
	}
	public String getVMInstallName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(
		IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
		(String) null);
	}
*/
	public IVMInstallType getVMInstallType(ILaunchConfiguration configuration)
	throws CoreException {
		String id = getVMInstallTypeId(configuration);
		if (id != null) {
			IVMInstallType type = JavaRuntime.getVMInstallType(id);
			if (type != null) {
				return type;
			}
		}
		return null;
	}
	
	public String getVMInstallTypeId(ILaunchConfiguration configuration)
	throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE,
				(String) null);
	}
	
	protected void abort(String message, Throwable exception, int code)
	throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin
				.getUniqueIdentifier(), code, message, exception));
	}
	
	public IVMInstall verifyVMInstall(ILaunchConfiguration configuration)
	throws CoreException {
		IVMInstall vm = getVMInstall(configuration);
		if (vm == null) {
			abort(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_The_specified_JRE_installation_does_not_exist_4, 
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		File location = vm.getInstallLocation();
		if (location == null) {
			abort(
					MessageFormat
					.format(
							LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_not_specified_for__0__5, 
							new String[]{vm.getName()}),
							null,
							IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		if (!location.exists()) {
			abort(
					MessageFormat
					.format(
							LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_for__0__does_not_exist___1__6, 
							new String[]{vm.getName(),
									location.getAbsolutePath()}),
									null,
									IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		return vm;
	}
	
	public String getVMConnectorId(ILaunchConfiguration configuration)
	throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
				(String) null);
	}
	public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) throws CoreException {
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			abort(MessageFormat.format(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_0, new String[]{vm.getName(), mode}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST); 
		}
		return runner;
	}
	
	public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
	}
}
