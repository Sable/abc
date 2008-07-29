package org.jastadd.plugin.jastadd;

import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.jastadd.plugin.save.JastAddSaveParticipant;
import org.osgi.framework.BundleContext;


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
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ISaveParticipant saveParticipant = new JastAddSaveParticipant();
        ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
	}
	
}
