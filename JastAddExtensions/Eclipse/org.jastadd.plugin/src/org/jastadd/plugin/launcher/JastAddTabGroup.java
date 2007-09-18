package org.jastadd.plugin.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.debug.ui.CommonTab;
import org.jastadd.plugin.launcher.classpaths.JastAddClasspathTab;


public class JastAddTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new JastAddMainTab(),
			new JavaArgumentsTab(),
		//	new JavaJRETab(),
		    new JastAddClasspathTab(), //	new JavaClasspathTab(),
		//	new SourceLookupTab(),	
			new CommonTab()
		};
		setTabs(tabs);
	}
}
