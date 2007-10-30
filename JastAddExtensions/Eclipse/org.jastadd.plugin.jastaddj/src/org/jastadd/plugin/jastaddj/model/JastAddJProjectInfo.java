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
		checkReloadBuildConfiguration(event, delta);
	}

	public synchronized JastAddJBuildConfiguration loadBuildConfiguration()
			throws CoreException {
		if (buildConfiguration == null) {
			if (buildConfigurationCoreException == null)
				return reloadBuildConfiguration();
			else
				throw buildConfigurationCoreException;
		} else
			return buildConfiguration;
	}

	public synchronized JastAddJBuildConfiguration reloadBuildConfiguration()
			throws CoreException {
		try {
			JastAddJBuildConfiguration newBuildConfiguration = getEmptyBuildConfiguration();
			doLoadBuildConfiguration(newBuildConfiguration);
			this.buildConfiguration = newBuildConfiguration;
			this.buildConfigurationCoreException = null;
			return buildConfiguration;
		} catch (Exception e) {
			this.buildConfiguration = null;
			this.buildConfigurationCoreException = model.makeCoreException(e,
					"Parsing build configuration failed: " + e.getMessage());
			throw this.buildConfigurationCoreException;
		}
	}

	public JastAddJBuildConfiguration getDefaultBuildConfiguration() {
		JastAddJBuildConfiguration buildConfiguration = getEmptyBuildConfiguration();
		JastAddJBuildConfigurationUtil.populateDefaults(buildConfiguration);
		return buildConfiguration;
	}

	public void saveBuildConfiguration(
			JastAddJBuildConfiguration buildConfiguration) throws CoreException {
		try {
			doSaveBuildConfiguration(buildConfiguration);
		} catch (Exception e) {
			throw model.makeCoreException(e,
					"Saving build configuration failed: " + e.getMessage());
		}
	}

	protected JastAddJBuildConfiguration getEmptyBuildConfiguration() {
		return new JastAddJBuildConfiguration();
	}

	protected void doLoadBuildConfiguration(
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.readBuildConfiguration(
				this.getProject(), buildConfiguration);
	}

	protected void doSaveBuildConfiguration(
			JastAddJBuildConfiguration buildConfiguration) throws Exception {
		JastAddJBuildConfigurationUtil.writeBuildConfiguration(getProject(),
				buildConfiguration);
	}

	protected void checkReloadBuildConfiguration(IResourceChangeEvent event,
			IResourceDelta delta) {
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			IResourceDelta newDelta = delta.findMember(new Path(
					JastAddJBuildConfigurationUtil.RESOURCE));
			if (newDelta != null)
				try {
					reloadBuildConfiguration();
				} catch (CoreException e) {
				}
			break;
		}
	}	
}
