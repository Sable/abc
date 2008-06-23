package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;

class EditSourcePathEntryDialog extends TrayDialog {
	private final SourcePathEntry sourcePathEntry;
	private final boolean addNew;
	private final IProject project;

	private Button okButton;
	private Button cancelButton;

	EditSourcePathEntryDialog(Shell parentShell, IProject project, SourcePathEntry sourcePathEntry,	boolean addNew) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.project = project;
		this.sourcePathEntry = sourcePathEntry != null ? sourcePathEntry : new SourcePathEntry();
		this.addNew = addNew;
	}
	
	void update(SourcePathEntry realSourcePathEntry) {
		realSourcePathEntry.update(sourcePathEntry);
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(addNew ? "New Source Path" : "Edit Source Path");
	}

	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected Control createDialogArea(final Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		Label sourcePathLabel = new Label(composite, SWT.LEFT);
		sourcePathLabel.setText("&Source path:");
		
		Composite sourcePathComposite = new Composite(composite, SWT.NONE);
		sourcePathComposite.setFont(parent.getFont());
		GridLayout sourcePathLayout = new GridLayout(2, false);
		sourcePathLayout.marginWidth = 0;
		sourcePathLayout.marginHeight = 0;
		sourcePathComposite.setLayout(sourcePathLayout);
		
		final Text sourcePathText = new Text(sourcePathComposite, SWT.BORDER);
		sourcePathText.setFont(sourcePathComposite.getFont());
		sourcePathText.setText(sourcePathEntry.sourcePath);
		sourcePathText.setLayoutData(UIUtil.suggestCharWidth(UIUtil.stretchControlHorizontal(new GridData()), sourcePathComposite, 50));		
		sourcePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				sourcePathEntry.sourcePath = sourcePathText.getText();
			}
		});

		Button button = new Button(sourcePathComposite, SWT.PUSH);
		button.setText("...");
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				ElementTreeSelectionDialog folderSelectionDialog = new ElementTreeSelectionDialog(parent.getShell(), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), getResourceProvider(IResource.FOLDER));
				folderSelectionDialog.setInput(project);

				// Set dialog title and text.
				folderSelectionDialog.setTitle("Source folder selection");
				folderSelectionDialog.setMessage("Select the source folder:");
				
				// Set initial selection
				IPath currentPath = new Path(project.getLocation().addTrailingSeparator() + sourcePathText.getText());
				currentPath = currentPath.removeFirstSegments(project.getLocation().segmentCount());
				if (project.exists(currentPath)) {
					folderSelectionDialog.setInitialSelection(project.findMember(currentPath));
				}
				
				folderSelectionDialog.setAllowMultiple(false);
				if (folderSelectionDialog.open() == IDialogConstants.OK_ID) {
				Object[] folders = folderSelectionDialog.getResult();
					if (folders != null && folders.length == 1) {
						IFolder folder = (IFolder) folders[0];
						sourcePathText.setText(folder.getProjectRelativePath().addTrailingSeparator().toString());
					}
				}
			}
		});

		Label includeLabel = new Label(composite, SWT.LEFT);
		includeLabel.setText("&Include patterns:");
		new PatternListEditField(sourcePathEntry.includeList, new String[] {"&Add", "E&dit", "&Remove"}).getControl(composite);

		Label excludeLabel = new Label(composite, SWT.LEFT);
		excludeLabel.setText("&Exclude patterns:");
		new PatternListEditField(sourcePathEntry.excludeList, new String[] {"A&dd", "Edi&t", "Rem&ove"}).getControl(composite);

		applyDialogFont(composite);
		return composite;
	}
	
    /**
     * Returns a content provider for <code>IResource</code>s that returns 
     * only children of the given resource type.
     */
    private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        //just return an empty set of children
                        return new Object[0];
                    }

                    //filter out the desired resource types
                    ArrayList<IResource> results = new ArrayList<IResource>();
                    for (int i = 0; i < members.length; i++) {
                        //And the test bits with the resource types to see if they are what we want
                        if ((members[i].getType() & resourceType) > 0) {
                            results.add(members[i]);
                        }
                    }
                    return results.toArray();
                }
                //input element case
                if (o instanceof ArrayList) {
                    return ((ArrayList) o).toArray();
                } 
                return new Object[0];
            }
        };
    }
	

	class PatternListEditField extends ListEditField<Pattern> {
		String[] labels;
		
		PatternListEditField(List<Pattern> data, String[] labels) {
			super(data, 50, 5);
			this.labels = labels;
		}
		
		protected String formatElement(Object element) {
			return ((JastAddJBuildConfiguration.Pattern)element).value.toString();
		}

		protected AddEditRemoveField buildAddEditRemoveField(final StructuredViewer dataViewer) {
			return new AddEditRemoveField(dataViewer, labels) {
				protected boolean hasSelection() {
					return !dataViewer.getSelection().isEmpty();
				}

				public Pattern getSelection() {
					IStructuredSelection selection = (IStructuredSelection) dataViewer
							.getSelection();
					if (selection.isEmpty())
						return null;
					return ((JastAddJBuildConfiguration.Pattern) selection
							.getFirstElement());
				}

				protected void addCommand() {
					PatternEditDialog dialog = new PatternEditDialog(null,
							true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Pattern pattern = new Pattern();
						dialog.update(pattern);
						getData().add(pattern);
						dataViewer.refresh();
					}
				}

				protected void editCommand() {
					JastAddJBuildConfiguration.Pattern pattern = getSelection();
					PatternEditDialog dialog = new PatternEditDialog(
							pattern, false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						dialog.update(pattern);
						dataViewer.refresh(pattern);
					}
				}

				protected void removeCommand() {
					Pattern pattern = getSelection();
					getData().remove(pattern);
					dataViewer.refresh();
				}
			};
		}
	}

	class PatternEditDialog extends InputDialog {
		PatternEditDialog(JastAddJBuildConfiguration.Pattern pattern,
				boolean addNew) {
			super(EditSourcePathEntryDialog.this.getParentShell(),
					addNew ? "Add Pattern" : "Edit Pattern", "Pattern:",
					addNew ? null : pattern.value, new IInputValidator() {
				public String isValid(String value) {
					if (value.length() == 0)
						return "Pattern should not be empty.";
					else
						return null;
				}
			});
		}

		public void update(Pattern pattern) {
			pattern.value = getValue();
		}
	}
}
