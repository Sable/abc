package org.jastadd.plugin;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public abstract class BaseJastAddActivator extends AbstractUIPlugin {
	protected JastAddModel model;
	protected List<Runnable> stopHandlers = new ArrayList<Runnable>();
	
	public abstract String getPluginID();
	
	public void addStopHandler(Runnable stopHandler) {
		synchronized(stopHandlers) {
			stopHandlers.add(stopHandler);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
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
		if (model != null) {
			JastAddModelProvider.removeModel(model);
			model = null;
		}
	}
}
