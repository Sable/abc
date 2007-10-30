package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.osgi.framework.BundleContext;

public class Activator extends org.jastadd.plugin.jastaddj.JastAddJActivator {
	public static String JASTADD_PLUGIN_ID = "org.jastadd.plugin.jastadd";
	public static Activator INSTANCE;
	
	public Activator() {
		INSTANCE = this;
	}	
	
	public String getPluginID() {
		return JASTADD_PLUGIN_ID;
	}	
}
