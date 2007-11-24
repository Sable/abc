package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class JastAddJLaunchShortcut implements ILaunchShortcut {
	public void launch(ISelection selection, String mode) {
		reportCreateLaunchConfiguration();
	}
	
	public void launch(IEditorPart editor, String mode) {
		reportCreateLaunchConfiguration();
	}
	
	private void reportCreateLaunchConfiguration() {
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Create Launch Configuration", "Please create a JastAddJ launch configuration!");
	}
}
