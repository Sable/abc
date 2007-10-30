package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.SWT;
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
	protected IProject project;
	protected JastAddJModel model;
	protected JastAddJProjectInfo projectInfo;
	protected JastAddJBuildConfiguration buildConfiguration;

	protected boolean needsSave = false;

	protected Text classPathControl;
	protected Text outputFolderControl;
		
	protected List<IPage> pages;
	
	public static interface IPage {
		public String getTitle();
		public Control getControl(Composite composite);
		public boolean hasChanges();
		public boolean updateBuildConfiguration();		
	}
		
	protected List<IPage> buildPages() {
		List<IPage> list = new ArrayList<IPage>();
		list.add(new SourcePathPage(getShell(), buildConfiguration));
		list.add(new ClassPathPage(getShell(), buildConfiguration));
		return list;
	}
	
	protected Control createContents(Composite parent) {
		this.noDefaultAndApplyButton();

		// Collect data
		project = (IProject) getElement().getAdapter(IProject.class);
		model = JastAddModelProvider.getModel(project, JastAddJModel.class);
		projectInfo = (JastAddJProjectInfo) JastAddModelProvider
				.getProjectInfo(model, project);
		try {
			buildConfiguration = projectInfo.reloadBuildConfiguration().copy();
		}
		catch(CoreException e) {
			buildConfiguration = projectInfo.getDefaultBuildConfiguration();
			needsSave = true;
		}

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
		
		pages = buildPages();
		for(IPage page : pages) {
			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(page.getTitle());
			item.setControl(page.getControl(folder));
		}

		return composite;
	}

	public boolean performOk() {
		boolean needsSave = this.needsSave;
		for(IPage page : pages)
			needsSave = needsSave || page.hasChanges();
			
		if (needsSave) {
			for(IPage page : pages)
				if (!page.updateBuildConfiguration())
					return false;
			
			try {
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor)
							throws CoreException, OperationCanceledException {
						projectInfo.saveBuildConfiguration(buildConfiguration);
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
