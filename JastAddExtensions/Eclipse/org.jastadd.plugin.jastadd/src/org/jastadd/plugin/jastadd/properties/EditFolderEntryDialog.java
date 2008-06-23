package org.jastadd.plugin.jastadd.properties;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastadd.properties.FolderList.FolderEntry;
import org.jastadd.plugin.jastadd.properties.FolderList.Pattern;
import org.jastadd.plugin.jastaddj.builder.ui.AddEditRemoveField;
import org.jastadd.plugin.jastaddj.builder.ui.ListEditField;
import org.jastadd.plugin.jastaddj.builder.ui.UIUtil;

class EditFolderEntryDialog extends TrayDialog {
	private final FolderEntry folderEntry;
	private final boolean addNew;
	private final IProject project;
	private String title;
	private Text sourcePathText;
	private String filter;

	public EditFolderEntryDialog(Shell parentShell, IProject project, FolderEntry folderEntry, String filter, String type, boolean addNew) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.project = project;
		this.title = type;
		this.folderEntry = folderEntry != null ? folderEntry : new FolderEntry();
		this.addNew = addNew;
		this.filter = filter;
	}
	
	void update(FolderEntry realSourcePathEntry) {
		realSourcePathEntry.update(folderEntry);
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(addNew ? "New " + title + " Path" : "Edit " + title + " Path");
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected Control createDialogArea(final Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		Label sourcePathLabel = new Label(composite, SWT.LEFT);
		sourcePathLabel.setText("&" + title + " path:");
		
		Composite sourcePathComposite = new Composite(composite, SWT.NONE);
		sourcePathComposite.setFont(parent.getFont());
		GridLayout sourcePathLayout = new GridLayout(2, false);
		sourcePathLayout.marginWidth = 0;
		sourcePathLayout.marginHeight = 0;
		sourcePathComposite.setLayout(sourcePathLayout);
		
		sourcePathText = new Text(sourcePathComposite, SWT.BORDER);
		sourcePathText.setFont(sourcePathComposite.getFont());
		sourcePathText.setText(folderEntry.getPath());
		sourcePathText.setLayoutData(UIUtil.suggestCharWidth(UIUtil.stretchControlHorizontal(new GridData()), sourcePathComposite, 50));		
		sourcePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Keep the temporary folder entry up-to-date
				folderEntry.setPath(sourcePathText.getText());
			}
		});
			
		Button button = new Button(sourcePathComposite, SWT.PUSH);
		button.setText("...");
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog folderSelectionDialog = new DirectoryDialog(parent.getShell());
				
				// Set dialog title
				folderSelectionDialog.setText(title + " folder selection");
				
				// Get the absolute path from the source text
				folderSelectionDialog.setFilterPath(FileFolderUtils.absolutePath(sourcePathText.getText(), project));
				
				// Retrieve user input
				String newDirectory = folderSelectionDialog.open();

				// If a directory was selected (null represents a canceled dialog)
				if (newDirectory != null) {
					
					// We want the folder to be relative to the project directory
					// We currently have an absolute path
					IPath base = project.getLocation().addTrailingSeparator();
					IPath target = new Path(newDirectory);
					
					String relativeDirectory = FileFolderUtils.relativize(base, target);
					
					sourcePathText.setText(relativeDirectory);
				}
			}
		});

		Label includeLabel = new Label(composite, SWT.LEFT);
		includeLabel.setText("&Include patterns:");
		new PatternListEditField(folderEntry.getIncludeList(), new String[] {"&Add", "E&dit", "&Remove"}).getControl(composite);

		Label excludeLabel = new Label(composite, SWT.LEFT);
		excludeLabel.setText("&Exclude patterns:");
		new PatternListEditField(folderEntry.getExcludeList(), new String[] {"A&dd", "Edi&t", "Rem&ove"}).getControl(composite);

		Label fileLabel = new Label(composite, SWT.LEFT);
		fileLabel.setText("&File patterns:");
		new FileListEditField(folderEntry.getFileList(), new String[] {"Add &file", "Ed&it", "Remo&ve"}).getControl(composite);
		
		applyDialogFont(composite);
		return composite;
	}
	
	class PatternListEditField extends ListEditField<Pattern> {
		String[] labels;
		
		PatternListEditField(List<Pattern> data, String[] labels) {
			super(data, 50, 5);
			this.labels = labels;
		}
		
		protected String formatElement(Object element) {
			return ((Pattern)element).value.toString();
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
					return ((Pattern) selection.getFirstElement());
				}
				
				public List<Pattern> getSelections() {
					IStructuredSelection selection = (IStructuredSelection) dataViewer
							.getSelection();
					if (selection.isEmpty())
						return null;
					return ((List<Pattern>) selection.toList());
				}

				protected void addCommand() {
					PatternEditDialog dialog = new PatternEditDialog(null, true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Pattern pattern = new Pattern(null);
						dialog.update(pattern);
						getData().add(pattern);
						dataViewer.refresh();
					}
				}

				protected void editCommand() {
					Pattern pattern = getSelection();
					PatternEditDialog dialog = new PatternEditDialog(pattern, false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						dialog.update(pattern);
						dataViewer.refresh(pattern);
					}
				}

				protected void removeCommand() {
					for (Pattern pattern : getSelections()) {
						getData().remove(pattern);
					}
					dataViewer.refresh();
				}
			};
		}
	}

	class FileListEditField extends ListEditField<Pattern> {
		String[] labels;
		
		FileListEditField(List<Pattern> data, String[] labels) {
			super(data, 50, 5);
			this.labels = labels;
		}
		
		protected String formatElement(Object element) {
			return ((Pattern)element).value.toString();
		}

		protected AddEditRemoveReorderField buildAddEditRemoveField(final StructuredViewer dataViewer) {
			return new AddEditRemoveReorderField(dataViewer, labels, false) {
				protected boolean hasSelection() {
					return !dataViewer.getSelection().isEmpty();
				}

				public Pattern getSelection() {
					IStructuredSelection selection = (IStructuredSelection) dataViewer
							.getSelection();
					if (selection.isEmpty())
						return null;
					return ((Pattern) selection.getFirstElement());
				}
				
				public List<Pattern> getSelections() {
					IStructuredSelection selection = (IStructuredSelection) dataViewer
							.getSelection();
					if (selection.isEmpty())
						return null;
					return ((List<Pattern>) selection.toList());
				}

				protected void addCommand() {
				
					FileDialog dialog = new FileDialog(EditFolderEntryDialog.this.getParentShell(), SWT.MULTI);

					String absoluteDirectory = setupAddEditDialog(dialog, "");
					
					String newFile = dialog.open();
					if (newFile != null) {
						// If we've changed the directory, we want to create
						// a relative path to the current directory
						String prefix = "";
						Path absolutePath = new Path(absoluteDirectory);
						Path newPath = new Path(dialog.getFilterPath());
						if (!absolutePath.equals(newPath)) {
							prefix = FileFolderUtils.relativize(absolutePath,newPath);
						}
						for (String filePattern : dialog.getFileNames()) {
							Pattern pattern = new Pattern(prefix + filePattern);
							getData().add(pattern);	
						}
						
						dataViewer.refresh();
					}
				}

				private String setupAddEditDialog(FileDialog dialog, String fileName) {
					// Set dialog title and text.
					dialog.setText(title + " file selection");

					// Set filters
					dialog.setFilterExtensions(new String[]{filter, "*.*"});
					dialog.setFilterNames(new String[]{title + " Files", "All files"});
					
					// Get the absolute path of the directory of this entry
					String sourceText = sourcePathText.getText();
					String absoluteDirectory = FileFolderUtils.absolutePath(sourceText, project);
										
					// Get the relative path of the file and, if it exists, resolve it against
					// the base directory.
					Path filePath = new Path(fileName);
					String file = filePath.lastSegment();
					if (file != null) {
						if (filePath.isAbsolute()) {
							// If the file already set is an absolute path, just use that
							dialog.setFilterPath(filePath.removeLastSegments(1).toString());
						} else {
							// Otherwise calculate a new absolute path from the directory and the relative section
							dialog.setFilterPath(FileFolderUtils.absolutePath(absoluteDirectory + filePath.removeLastSegments(1), project));
						}
						dialog.setFileName(file);
					} else {
						dialog.setFilterPath(absoluteDirectory);
					}
					return absoluteDirectory;
				}

				protected void editCommand() {
					Pattern pattern = getSelection();
					
					FileDialog dialog = new FileDialog(EditFolderEntryDialog.this.getParentShell(), SWT.OPEN);
					
					String absoluteDirectory = setupAddEditDialog(dialog, pattern.value);
					
					String newFile = dialog.open();
					if (newFile != null) {
						// If we've changed the directory, we want to create
						// a relative path to the current directory
						String prefix = "";
						Path absolutePath = new Path(absoluteDirectory);
						Path newPath = new Path(dialog.getFilterPath());
						if (!absolutePath.equals(newPath)) {
							prefix = FileFolderUtils.relativize(absolutePath,newPath);
						}
						pattern.update(new Pattern(prefix + dialog.getFileName()));
						
						dataViewer.refresh(pattern);
					}

				}

				protected void removeCommand() {
					for (Pattern pattern : getSelections()) {
						getData().remove(pattern);
					}
					dataViewer.refresh();
				}

				protected void addFileCommand() {
					//unused
				}

				@Override
				protected boolean bottomOfSelection() {
					Pattern file = getSelection();
					return file != null && folderEntry.getFileList().contains(file) && folderEntry.getFileList().indexOf(file) == (folderEntry.getFileList().size() - 1);
				}

				@Override
				protected void downCommand() {
					Pattern file = getSelection();
					folderEntry.fileDown(file);
					dataViewer.refresh();
				}

				@Override
				protected boolean topOfSelection() {
					Pattern file = getSelection();
					return file != null && folderEntry.getFileList().contains(file) && folderEntry.getFileList().indexOf(file) == 0;
				}

				@Override
				protected void upCommand() {
					Pattern file = getSelection();
					folderEntry.fileUp(file);
					dataViewer.refresh();
				}
			};
		}
	}
	
	class PatternEditDialog extends InputDialog {
		PatternEditDialog(Pattern pattern, boolean addNew) {
			super(EditFolderEntryDialog.this.getParentShell(),
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
