package org.jastadd.plugin.jastadd;


public class Activator extends org.jastadd.plugin.BaseJastAddActivator {
	public static String JASTADD_PLUGIN_ID = "org.jastadd.plugin.jastadd";
	public static Activator INSTANCE;
	
	public Activator() {
		INSTANCE = this;
	}
	
	public static Activator getInstance() {
		return INSTANCE;
	}
	
	public String getPluginID() {
		return JASTADD_PLUGIN_ID;
	}	
}
