package org.jastadd.plugin.jastadd.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastadd.properties.JastAddBuildConfiguration.PackageEntry;
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;
import org.jastadd.plugin.jastaddj.builder.ui.JastAddJBuildConfigurationPropertyPage.IPage;

public class JastAddFilesPropertyPage implements IPage {
	private Shell shell;
	private PackageEntry packageEntry;
	private String title;
	private boolean hasChanges;
	private IProject project;

	JastAddFilesPropertyPage(Shell shell, IProject project, PackageEntry packageEntry) {
		this.shell = shell;
		this.packageEntry = packageEntry;
		this.hasChanges = false;
		this.project = project;
		this.title = "JastAdd";
	}
	
	@Override	
	public String getTitle() {
		return "&" + title + " Files";
	}
	
	@Override
	public Control getControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Label titleLabel = new Label(composite, SWT.LEFT);
		titleLabel.setText(title + " File&s:");
				
		Composite packageComposite = new Composite(composite, SWT.NONE);
		packageComposite.setFont(parent.getFont());
		GridLayout packageCompositeLayout = new GridLayout(3, false);
		packageCompositeLayout.marginWidth = 0;
		packageCompositeLayout.marginHeight = 0;
		packageComposite.setLayout(packageCompositeLayout);
		
		Label packageLabel = new Label(packageComposite, SWT.NONE);
		packageLabel.setText("&Package:");

		final Text packageControl = new Text(packageComposite, SWT.SINGLE | SWT.BORDER);
		packageControl.setFont(parent.getFont());
		if (packageEntry.getPackage() != null)
			packageControl.setText(packageEntry.getPackage());
		packageControl.setLayoutData(UIUtil.suggestCharWidth(UIUtil. stretchControlHorizontal(new GridData()), parent, 50));		
		packageControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = packageControl.getText();
				if (text.length() > 0)
					packageEntry.setPackage(text);
				else
					packageEntry.setPackage(null);
				hasChanges = true;
			}
		});
		
		final Button browseButton = new Button(packageComposite, SWT.RIGHT);
		SelectionAdapter adapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					DirectoryDialog dialog = new DirectoryDialog (shell);
					IPath path = project.getRawLocation();
					if (path == null) {
						IPath projectPath = project.getFullPath();
						path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().append(projectPath);
					}
					String projectDir = path.append("/src").toOSString();
					dialog.setFilterPath(projectDir);
					String dir = dialog.open();
				    if (dir != null) {
				    	if (dir.contains(dir)) {
				    		dir = dir.substring(projectDir.length() + 1);
				    	}
				    	dir = dir.replace(IPath.SEPARATOR, '.');
				    	packageControl.setText(dir);
				    }
				}
		};
		browseButton.addSelectionListener(adapter);
		browseButton.setText("&Browse...");
		GridData browseControlGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		browseButton.setLayoutData(browseControlGridData);
		
		return composite;
	}


	@Override
	public boolean hasChanges() {
		return hasChanges;
	}

	@Override
	public boolean updateBuildConfiguration() {
		return true;
	}

}
