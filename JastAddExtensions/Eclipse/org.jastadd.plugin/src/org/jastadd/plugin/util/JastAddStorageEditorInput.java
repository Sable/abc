package org.jastadd.plugin.util;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class JastAddStorageEditorInput extends PlatformObject implements
		IStorageEditorInput {

	private IProject project;
	private IStorage storage;
	//private JastAddModel model;

	public JastAddStorageEditorInput(IProject project, IStorage storage) {
			//JastAddModel model) {
		this.project = project;
		this.storage = storage;
		//this.model = model;
	}

	public IProject getProject() {
		return project;
	}

	public IStorage getStorage() {
		return storage;
	}

	/*
	public JastAddModel getModel() {
		return model;
	}
	*/

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return getStorage().getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getStorage().getFullPath().toOSString();
	}

	public boolean equals(Object object) {
		if (!(object instanceof JastAddStorageEditorInput))
			return false;
		JastAddStorageEditorInput editorInput = (JastAddStorageEditorInput) object;
		return getProject().equals(editorInput.getProject())
				//&& getModel() == editorInput.getModel()
				&& getStorage().getFullPath().equals(editorInput.getStorage().getFullPath());
	}

	public int hashCode() {
		return Arrays.hashCode(new Object[]{getProject(), getStorage()});
	}

	public boolean exists() {
		return true;
	}
}