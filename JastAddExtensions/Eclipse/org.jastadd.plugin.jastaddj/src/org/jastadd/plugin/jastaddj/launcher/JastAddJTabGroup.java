package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;


public class JastAddJTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		/*
		JastAddJModel model = null;
		for (JastAddModel m : JastAddModelProvider.getModels()) {
			if (m instanceof JastAddJModel) {
				model = (JastAddJModel)m;
			}
		}
		*/
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new JastAddJMainTab(),
			new JavaArgumentsTab(),
		//	new JavaJRETab(),
		//    new JastAddJClasspathTab(), //	new JavaClasspathTab(),
		//	new SourceLookupTab(),	
			new CommonTab()
		};
		setTabs(tabs);
	}
}
