package org.jastadd.plugin.jastaddj;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.jastadd.plugin.util.BaseResourceChangeListener;

public class JastAddJResourceChangeListener extends BaseResourceChangeListener {

	@Override
	protected void projectResourceChanged(IProject project,
			IResourceChangeEvent event, IResourceDelta delta) {
		super.projectResourceChanged(project, event, delta);
		
		
	}
}
