package org.jastadd.plugin.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class RefactoringSaveHelper {

	public static boolean makeSureEditorsSaved(Shell shell) {
		if (unsavedEditorsExist())
			if (askSaveAllEditors(shell))
				return saveAllEditors();
			else
				return false;
		else
			return true;
	}

	public static boolean unsavedEditorsExist() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorPart[] editors = pages[x].getDirtyEditors();
				if (editors.length > 0)
					return true;
			}
		}
		return false;
	}

	public static boolean askSaveAllEditors(Shell shell) {
		return MessageDialog
				.openConfirm(shell, "Refactoring: Unsaved Files",
						"Refactoring cannot proceed with unsaved files. Save all files?");
	}

	public static boolean saveAllEditors() {
		return PlatformUI.getWorkbench().saveAllEditors(false);
	}
}
