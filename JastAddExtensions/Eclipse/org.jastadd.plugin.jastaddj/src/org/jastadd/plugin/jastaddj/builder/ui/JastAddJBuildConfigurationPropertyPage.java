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
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.util.BuildUtil;
import org.jastadd.plugin.jastaddj.util.WorkspaceUtil;

public class JastAddJBuildConfigurationPropertyPage extends PropertyPage {
	protected IProject project;
	//protected JastAddJModel model;
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
		
	
	protected void addPages(List<IPage> pageList) {
		pageList.add(new SourcePathPage(getShell(), project, buildConfiguration));
		pageList.add(new ClassPathPage(getShell(), buildConfiguration));
	}
	
	protected void initContents() {
		// Collect data
		project = (IProject) getElement().getAdapter(IProject.class);
		//model = JastAddModelProvider.getModel(project, JastAddJModel.class);
		try {
			buildConfiguration = BuildUtil.readBuildConfiguration(project).copy();
		}
		catch(CoreException e) {
			buildConfiguration = BuildUtil.getDefaultBuildConfiguration();
			needsSave = true;
		}
	}

	protected void doSave() throws CoreException {
		BuildUtil.writeBuildConfiguration(project, buildConfiguration);
	}

	
	protected List<IPage> buildPages() {
		List<IPage> list = new ArrayList<IPage>();
		addPages(list);
		return list;
	}
	
	protected Control createContents(Composite parent) {
		this.noDefaultAndApplyButton();

		initContents();
	
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
						doSave();
					}
				}, new NullProgressMonitor());
			} catch (CoreException e) {
				WorkspaceUtil.displayError(e, getShell(), "Error!",
						"Failed saving build configuration!");
				return false;
			}			
		}
		return true;
	}
}
