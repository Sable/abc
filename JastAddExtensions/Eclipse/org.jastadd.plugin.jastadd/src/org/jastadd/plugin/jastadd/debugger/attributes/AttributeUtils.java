package org.jastadd.plugin.jastadd.debugger.attributes;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class AttributeUtils {

	/**
	 * Finds the project that holds the debug target for this variable.
	 */ 
	public static IProject getProject(IJavaValue value) throws CoreException {
		Map<String, String> launchAttributes = value.getDebugTarget().getLaunch().getLaunchConfiguration().getAttributes();
		String projectName = launchAttributes.get(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		return project;
	}

}
