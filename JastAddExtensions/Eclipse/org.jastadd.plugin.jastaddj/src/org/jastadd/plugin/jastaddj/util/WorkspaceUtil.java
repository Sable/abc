package org.jastadd.plugin.jastaddj.util;

import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class WorkspaceUtil {

	public static void displayError(Throwable t, Shell shell, String title, String message) {
		StringWriter msg= new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
			msg.write(t.getMessage());
		}
		MessageDialog.openError(shell, title, msg.toString());			
	}

}
