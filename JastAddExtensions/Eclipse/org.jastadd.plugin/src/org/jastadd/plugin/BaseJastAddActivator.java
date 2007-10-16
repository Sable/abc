package org.jastadd.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class BaseJastAddActivator extends AbstractUIPlugin {
	protected JastAddModel model;
	protected Collection<Command> commands = new ArrayList<Command>();

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
		synchronized(BaseJastAddActivator.this) {
			for(Command command : commands) 
				command.undefine();
		}
		if (model != null) {
			JastAddModelProvider.removeModel(model);
			model = null;
		}
	}
	
	protected void registerModelCommands(final JastAddModel model) {
		getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					synchronized(BaseJastAddActivator.this) {
						model.getEditorConfiguration().populateCommands(commands);
					}
				} catch (ParseException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});		
	}
}
