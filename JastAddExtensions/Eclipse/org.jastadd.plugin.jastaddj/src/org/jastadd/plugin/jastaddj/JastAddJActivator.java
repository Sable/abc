package org.jastadd.plugin.jastaddj;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jastadd.plugin.BaseJastAddActivator;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.osgi.framework.BundleContext;

public class JastAddJActivator extends BaseJastAddActivator {
	public static String JASTADDJ_PLUGIN_ID = "org.jastadd.plugin.jastaddj";

	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerModelCommands(new JastAddJModel());
	}
}
