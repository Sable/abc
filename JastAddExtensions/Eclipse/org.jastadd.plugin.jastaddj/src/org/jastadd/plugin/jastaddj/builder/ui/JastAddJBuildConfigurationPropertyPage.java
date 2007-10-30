package org.jastadd.plugin.jastaddj.builder.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfigurationUtil;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.jastaddj.model.JastAddJProjectInfo;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddJBuildConfigurationPropertyPage extends PropertyPage {

	public static final String PROP_ID = "org.jastadd.plugin.jastaddj.ui.propertyPages.BuildConfigPropertyPage"; //$NON-NLS-1$

	protected IProject project;
	protected JastAddJModel model;
	protected JastAddJProjectInfo projectInfo;
	protected JastAddJBuildConfiguration buildConfiguration;

	protected boolean hasChanges = false;

	protected ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			hasChanges = true;
		}
	};

	protected Text classPathControl;
	protected Text outputFolderControl;

	protected Control createContents(Composite parent) {
		this.noDefaultAndApplyButton();

		// Collect data
		project = (IProject) getElement().getAdapter(IProject.class);
		model = JastAddModelProvider.getModel(project, JastAddJModel.class);
		projectInfo = (JastAddJProjectInfo) JastAddModelProvider
				.getProjectInfo(model, project);
		buildConfiguration = projectInfo.reloadBuildConfiguration();
		if (buildConfiguration != null)
			buildConfiguration = buildConfiguration.copy();
		else
			buildConfiguration = JastAddJBuildConfigurationUtil
					.defaultBuildConfiguration(project);
		if (projectInfo.getBuildConfigurationException() != null)
			hasChanges = true;

		// Build UI
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		TabFolder folder = new TabFolder(composite, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setFont(composite.getFont());

		TabItem sourceItem = new TabItem(folder, SWT.NONE);
		sourceItem.setText("&Source Path");
		sourceItem.setControl(new SourcePathPage(this).getControl(folder));

		TabItem classPathItem = new TabItem(folder, SWT.NONE);
		classPathItem.setText("&Class Path");
		classPathItem.setControl(new ClassPathPage(this).getControl(folder));

		return composite;
	}

	public boolean performOk() {
		if (hasChanges) {
			try {
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor)
							throws CoreException, OperationCanceledException {
						JastAddJBuildConfigurationUtil.writeBuildConfiguration(
								project, buildConfiguration);
					}
				}, new NullProgressMonitor());
			} catch (CoreException e) {
				JastAddJActivator.displayError(e, getShell(), "Error!",
						"Failed saving build configuration!");
				return false;
			}			
		}
		return true;
	}
}
