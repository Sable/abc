package org.jastadd.plugin.save;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddSaveParticipant implements ISaveParticipant {

	@Override
	public void doneSaving(ISaveContext context) {
	}

	@Override
	public void prepareToSave(ISaveContext context) throws CoreException {
		if (context.getKind() == ISaveContext.SNAPSHOT) {
			IProject project = context.getProject();
			for(JastAddModel m : JastAddModelProvider.getModels(project)) {
				m.checkForErrors(project);
			}	
		}
	}

	@Override
	public void rollback(ISaveContext context) {
	}

	@Override
	public void saving(ISaveContext context) throws CoreException {
		System.out.println("JastAddSaveParticipant.saving");
	}
}
