package org.jastadd.plugin.jastaddj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.jastaddj.debugger.FilteredDebugElementAdapterFactory;
import org.jastadd.plugin.jastaddj.debugger.InterceptFactory;
import org.jastadd.plugin.jastaddj.debugger.JastAddJDebug;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static String JASTADDJ_PLUGIN_ID = "org.jastadd.plugin.jastaddj";
	public static Activator INSTANCE;
	
	public Activator() {
		INSTANCE = this;
	}
	
	public static Activator getInstance() {
		return INSTANCE;
	}

	public String getPluginID() {
		return JASTADDJ_PLUGIN_ID;
	}
	
	//protected JastAddModel model;
	protected List<Runnable> stopHandlers = new ArrayList<Runnable>();
	

	
	public void start(BundleContext context) throws Exception {
		// These two adapters override the default adapters for Label and Content providers
		// for the stack frame listing, allowing filtering and renaming of the stack frames.
		// By removing these lines, you can disable the stack frame filtering/renaming.
		
		IAdapterManager manager = Platform.getAdapterManager();

		// This provides filtering of the stack listing
		manager.registerAdapters(new InterceptFactory(), IJavaThread.class);
		
		// This provides a renaming facility on stack frame objects
		manager.registerAdapters(new FilteredDebugElementAdapterFactory(), IJavaStackFrame.class);
		
		DebugPlugin.getDefault().addDebugEventListener(new JastAddJDebug());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new JastAddJResourceChangeListener());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	/*
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		
		 *  TODO Add this again 
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
*/
	
	public void addStopHandler(Runnable stopHandler) {
		synchronized(stopHandlers) {
			stopHandlers.add(stopHandler);
		}
	}
	
}
