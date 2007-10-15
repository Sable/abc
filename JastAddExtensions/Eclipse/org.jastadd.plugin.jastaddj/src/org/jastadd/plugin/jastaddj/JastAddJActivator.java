package org.jastadd.plugin.jastaddj;

import org.jastadd.plugin.BaseJastAddActivator;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.osgi.framework.BundleContext;

public class JastAddJActivator extends BaseJastAddActivator {
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.model = JastAddModelProvider.getModel(JastAddJModel.class);
		registerModelCommands();
	}
}
