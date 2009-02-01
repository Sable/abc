package org.jastadd.plugin.jastadd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.util.JastAddSaveParticipant;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin {
	
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
	
	protected List<Runnable> stopHandlers = new ArrayList<Runnable>();
	
	public void addStopHandler(Runnable stopHandler) {
		synchronized(stopHandlers) {
			stopHandlers.add(stopHandler);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		synchronized(stopHandlers) {
			for(Runnable stopHandler : stopHandlers) {
				try {
					stopHandler.run();
				}
				catch(Throwable t) {
					getLog().log(new Status(IStatus.ERROR, getPluginID(), "Stop handler failed", t));
				}
			}
		}
	}
	
}
