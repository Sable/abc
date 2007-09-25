package org.jastadd.plugin.jastaddj;

import org.jastadd.plugin.JastAddActivator;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModel;

/**
 * The activator class controls the plug-in life cycle
 */
public class JastAddJActivator extends JastAddActivator {

	public static final String PLUGIN_ID = "org.jastadd.plugin.jastaddj";
	private static JastAddJActivator plugin;
	public static JastAddJActivator getDefault() {
		return plugin;
	}

	@Override
	protected JastAddModel createModel() {
		return new JastAddJModel();
	}
}
