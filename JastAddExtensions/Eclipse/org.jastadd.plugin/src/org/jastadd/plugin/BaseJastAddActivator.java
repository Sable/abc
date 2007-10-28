package org.jastadd.plugin;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class BaseJastAddActivator extends AbstractUIPlugin {
	public static BaseJastAddActivator JASTADD_INSTANCE = null;
	
	protected JastAddModel model;
	protected Collection<Command> commands = new ArrayList<Command>();

	public BaseJastAddActivator() {
		super();
		JASTADD_INSTANCE = this;
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
		synchronized(BaseJastAddActivator.this) {
			for(Command command : commands) 
				command.undefine();
		}
		if (model != null) {
			JastAddModelProvider.removeModel(model);
			model = null;
		}
	}
		
	public static void displayError(Throwable t, Shell shell, String title, String message) {
		StringWriter msg= new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
			msg.write(t.getMessage());
		}
		MessageDialog.openError(shell, title, msg.toString());			
	}	
	
	protected void registerModelCommands(final JastAddModel model) {
		/*getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {*/
				try {
					synchronized(BaseJastAddActivator.this) {
						model.getEditorConfiguration().populateCommands(commands);
					}
				} catch (ParseException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			/*}
		});	*/	
	}
}
