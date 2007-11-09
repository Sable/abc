package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.osgi.framework.BundleContext;

public class Activator extends org.jastadd.plugin.BaseJastAddActivator {
	public static String JASTADD_PLUGIN_ID = "org.jastadd.plugin.jastadd";
	public static Activator INSTANCE;
	
	public Activator() {
		INSTANCE = this;
	}
	
	public static Activator getInstnace() {
		return INSTANCE;
	}
	
	public String getPluginID() {
		return JASTADD_PLUGIN_ID;
	}	
}
