package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.ClassPathEntry;

class ClassPathPage implements JastAddJBuildConfigurationPropertyPage.IPage {
	private Shell shell;
	private JastAddJBuildConfiguration buildConfiguration;
	private boolean hasChanges;
	
	ClassPathPage(Shell shell, JastAddJBuildConfiguration buildConfiguration) {
		this.shell = shell;
		this.buildConfiguration = buildConfiguration;
		this.hasChanges = false;
	}
	
	public String getTitle() {
		return "&Class Path";
	}
	
	public Control getControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Label classTreeLabel = new Label(composite, SWT.LEFT);
		classTreeLabel.setText("Class pat&h:");
		new ClassPathEntryListEditField(buildConfiguration.classPathList, new String[] {"&Add", "&Edit", "&Remove"}).getControl(composite);

		return composite;
	}
	
	public boolean hasChanges() {
		return hasChanges;
	}
	
	public boolean updateBuildConfiguration() {
		return true;
	}

	class ClassPathEntryListEditField extends ListEditField<ClassPathEntry> {
		String[] labels;
		
		ClassPathEntryListEditField(List<ClassPathEntry> data, String[] labels) {
			super(data, 20, 15);
			this.labels = labels;
		}
		
		protected String formatElement(Object element) {
			ClassPathEntry classPathEntry = (ClassPathEntry)element;
			String text = classPathEntry.classPath;
			if (classPathEntry.sourceAttachmentPath != null)
				text = text + " (" + classPathEntry.sourceAttachmentPath + ")";
			return text;
		}

		protected AddEditRemoveField buildAddEditRemoveField(final StructuredViewer dataViewer) {
			return new AddEditRemoveField(dataViewer, labels) {
				protected boolean hasSelection() {
					return !dataViewer.getSelection().isEmpty();
				}

				public ClassPathEntry getSelection() {
					IStructuredSelection selection = (IStructuredSelection) dataViewer
							.getSelection();
					if (selection.isEmpty())
						return null;
					return ((JastAddJBuildConfiguration.ClassPathEntry) selection
							.getFirstElement());
				}

				protected void addCommand() {
					EditClassPathEntryDialog dialog = new EditClassPathEntryDialog(shell, null,
							true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						ClassPathEntry classPathEntry = new ClassPathEntry();
						dialog.update(classPathEntry);
						getData().add(classPathEntry);
						dataViewer.refresh();
						hasChanges = true;
					}
				}

				protected void editCommand() {
					JastAddJBuildConfiguration.ClassPathEntry classPathEntry = getSelection();
					EditClassPathEntryDialog dialog = new EditClassPathEntryDialog(shell, 
							classPathEntry, false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						dialog.update(classPathEntry);
						dataViewer.refresh(classPathEntry);
						hasChanges = true;
					}
				}

				protected void removeCommand() {
					ClassPathEntry classPathEntry = getSelection();
					getData().remove(classPathEntry);
					dataViewer.refresh();
					hasChanges = true;
				}
			};
		}
	}
}
