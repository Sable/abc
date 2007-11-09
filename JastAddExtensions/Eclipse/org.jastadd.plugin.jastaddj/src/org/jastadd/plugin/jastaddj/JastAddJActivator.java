package org.jastadd.plugin.jastaddj;

import org.jastadd.plugin.BaseJastAddActivator;

public class JastAddJActivator extends BaseJastAddActivator {
	public static String JASTADDJ_PLUGIN_ID = "org.jastadd.plugin.jastaddj";
	public static JastAddJActivator INSTANCE;
	
	public JastAddJActivator() {
		INSTANCE = this;
	}
	
	public static JastAddJActivator getInstance() {
		return INSTANCE;
	}
	
	public String getPluginID() {
		return JASTADDJ_PLUGIN_ID;
	}
}
