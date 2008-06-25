package org.jastadd.plugin.jastaddj;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.jastadd.plugin.BaseJastAddActivator;
import org.jastadd.plugin.jastaddj.debugger.FilteredDebugElementAdapterFactory;
import org.jastadd.plugin.jastaddj.debugger.InterceptFactory;
import org.jastadd.plugin.jastaddj.debugger.JastAddJDebug;
import org.osgi.framework.BundleContext;

public class JastAddJActivator extends BaseJastAddActivator {
	public static String JASTADDJ_PLUGIN_ID = "org.jastadd.plugin.jastaddj";
	public static JastAddJActivator INSTANCE;
	
	public JastAddJActivator() {
		INSTANCE = this;
	}
	
	public static JastAddJActivator getInstance() {
		return INSTANCE;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// These two adapters override the default adapters for Label and Content providers
		// for the stack frame listing, allowing filtering and renaming of the stackframes.
		// By removing these lines, you can disable the stackframe filtering/renaming.
		
		IAdapterManager manager = Platform.getAdapterManager();

		// This provides filtering of the stack listing
		manager.registerAdapters(new InterceptFactory(), IJavaThread.class);
		
		// This provides a renaming facility on stack frame objects
		manager.registerAdapters(new FilteredDebugElementAdapterFactory(), IJavaStackFrame.class);
		
		DebugPlugin.getDefault().addDebugEventListener(new JastAddJDebug());
	}
	
	public String getPluginID() {
		return JASTADDJ_PLUGIN_ID;
	}
}
