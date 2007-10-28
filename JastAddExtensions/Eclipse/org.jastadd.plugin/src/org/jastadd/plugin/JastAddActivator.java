package org.jastadd.plugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.model.JastAddProjectInfoRefresher;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JastAddActivator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.jastadd.plugin";
	
	public void start(BundleContext context) throws Exception {
		super.start(context);	
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new JastAddProjectInfoRefresher());
	}
}
