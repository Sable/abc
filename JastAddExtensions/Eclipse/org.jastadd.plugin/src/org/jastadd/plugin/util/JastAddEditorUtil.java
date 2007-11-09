package org.jastadd.plugin.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddEditorUtil {
	public static IEditorDescriptor getEditorDescription(IFile file) {
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		IEditorDescriptor selectedDescriptor = null;

		String defaultEditorID = null;
		try {
			defaultEditorID = file.getPersistentProperty(IDE.EDITOR_KEY);
		} catch (CoreException e) {
		}
		if (defaultEditorID != null)
			selectedDescriptor = editorReg.findEditor(defaultEditorID);

		if (selectedDescriptor == null) {
			JastAddModel model = JastAddModelProvider.getModel(file);
			if (model != null) {
				IEditorDescriptor[] descriptors = editorReg.getEditors(file
						.getName());
				for (IEditorDescriptor descriptor : descriptors)
					if (descriptor.getId().equals(model.getEditorID()))
						selectedDescriptor = descriptor;
				if (selectedDescriptor == null && descriptors.length > 0)
					selectedDescriptor = descriptors[0];
			}
		}
		return selectedDescriptor;
	}
}
