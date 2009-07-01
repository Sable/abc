package org.jastadd.plugin.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class JastAddSaveParticipant implements ISaveParticipant {

	public void doneSaving(ISaveContext context) {
		IProject project = context.getProject();
		for (IPath path : context.getFiles()) {
			IFile file = project.getFile(path);
			try {
				file.touch(null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
		/*
		System.out.println("JastAddSaveP..prepare");
		if (context.getKind() == ISaveContext.SNAPSHOT) {
			IProject project = context.getProject();
			for(JastAddModel m : JastAddModelProvider.getModels(project)) {
				m.checkForErrors(project);
			}	
		}
		*/
	}

	public void rollback(ISaveContext context) {
	}

	public void saving(ISaveContext context) throws CoreException {
		//System.out.println("JastAddSaveParticipant.saving");
	}
}
