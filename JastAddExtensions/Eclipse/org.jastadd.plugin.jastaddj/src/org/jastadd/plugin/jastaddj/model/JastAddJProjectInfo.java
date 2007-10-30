package org.jastadd.plugin.jastaddj.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfigurationUtil;
import org.jastadd.plugin.model.JastAddProjectInfo;

public class JastAddJProjectInfo extends JastAddProjectInfo {
	private JastAddJModel model;

	private CoreException buildConfigurationCoreException;
	private JastAddJBuildConfiguration buildConfiguration;

	public JastAddJProjectInfo(JastAddJModel model, IProject project) {
		super(model, project);
		this.model = model;
	}

	public JastAddJModel getJastAddJModel() {
		return model;
	}

	public void resourceChanged(IResourceChangeEvent event, IResourceDelta delta) {
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta newDelta = delta.findMember(new Path(
					JastAddJBuildConfigurationUtil.RESOURCE));
			if (newDelta != null)
				reloadBuildConfiguration();
			break;
		}
	}

	public CoreException getBuildConfigurationException() {
		return buildConfigurationCoreException;
	}

	public JastAddJBuildConfiguration loadBuildConfiguration() {
		if (buildConfiguration == null) {
			return reloadBuildConfiguration();
		}
		return buildConfiguration;
	}

	public JastAddJBuildConfiguration reloadBuildConfiguration() {
		buildConfigurationCoreException = null;
		JastAddJBuildConfiguration newBuildConfiguration = null;
		try {
			newBuildConfiguration = JastAddJBuildConfigurationUtil
					.readBuildConfiguration(this.getProject());
		} catch (Exception e) {
			buildConfigurationCoreException = model.makeCoreException(e, "Parsing build configuration '"
					+ JastAddJBuildConfigurationUtil.RESOURCE + "' failed: " + e.getMessage());
			model.logCoreException(buildConfigurationCoreException);
		}
		updateBuildConfiguration(newBuildConfiguration);
		return buildConfiguration;
	}

	private void updateBuildConfiguration(
			JastAddJBuildConfiguration buildConfiguration) {
		// TODO:
		// * Test that class path has changed indeed
		// * Notify the listeners, if changed
		this.buildConfiguration = buildConfiguration;
	}
}
