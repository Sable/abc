package org.jastadd.plugin.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.jastadd.plugin.launcher.classpaths.JastAddClasspathTab;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;


public class JastAddTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		JastAddModel model = null;
		for (JastAddModel m : JastAddModelProvider.getModels()) {
			if (m instanceof JastAddModel) {
				model = m;
			}
		}
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new JastAddMainTab(model),
			new JavaArgumentsTab(),
		//	new JavaJRETab(),
		    new JastAddClasspathTab(), //	new JavaClasspathTab(),
		//	new SourceLookupTab(),	
			new CommonTab()
		};
		setTabs(tabs);
	}
}
