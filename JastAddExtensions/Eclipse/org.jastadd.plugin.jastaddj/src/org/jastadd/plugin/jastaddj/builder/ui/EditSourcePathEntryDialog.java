package org.jastadd.plugin.jastaddj.builder.ui;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.Pattern;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;

class EditSourcePathEntryDialog extends TrayDialog {
	private final SourcePathEntry sourcePathEntry;
	private final boolean addNew;

	private Button okButton;
	private Button cancelButton;

	EditSourcePathEntryDialog(Shell parentShell, SourcePathEntry sourcePathEntry,
			boolean addNew) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
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

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		Label sourcePathLabel = new Label(composite, SWT.LEFT);
		sourcePathLabel.setText("&Source path:");

		final Text sourcePathText = new Text(composite, SWT.BORDER);
		sourcePathText.setFont(parent.getFont());
		sourcePathText.setText(sourcePathEntry.sourcePath);
		UIUtil.stretchControlHorizontal(sourcePathText);
		sourcePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				sourcePathEntry.sourcePath = sourcePathText.getText();
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

	class PatternListEditField extends ListEditField<Pattern> {
		String[] labels;
		
		PatternListEditField(List<Pattern> data, String[] labels) {
			super(data);
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
