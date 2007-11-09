package org.jastadd.plugin.jastaddj.builder.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.ClassPathEntry;

class EditClassPathEntryDialog extends TrayDialog {
	private final ClassPathEntry classPathEntry;
	private final boolean addNew;

	private Button okButton;
	private Button cancelButton;

	EditClassPathEntryDialog(Shell parentShell, ClassPathEntry classPathEntry,
			boolean addNew) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.classPathEntry = classPathEntry != null ? classPathEntry.copy() : new ClassPathEntry();
		this.addNew = addNew;
	}
	
	void update(ClassPathEntry realClassPathEntry) {
		realClassPathEntry.update(classPathEntry);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(addNew ? "New Class Path" : "Edit Class Path");
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

		Label classPathLabel = new Label(composite, SWT.LEFT);
		classPathLabel.setText("&Class path:");

		final Text classPathText = new Text(composite, SWT.BORDER);
		classPathText.setFont(parent.getFont());
		classPathText.setText(classPathEntry.classPath);
		classPathText.setLayoutData(UIUtil.suggestCharWidth(UIUtil.stretchControlHorizontal(new GridData()), parent, 50));
		classPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				classPathEntry.classPath = classPathText.getText();
			}
		});

		Label sourceAttachmentPathLabel = new Label(composite, SWT.LEFT);
		sourceAttachmentPathLabel.setText("&Source attachment path:");

		final Text sourceAttachmentPathText = new Text(composite, SWT.BORDER);
		sourceAttachmentPathText.setFont(parent.getFont());
		if (classPathEntry.sourceAttachmentPath != null)
			sourceAttachmentPathText.setText(classPathEntry.sourceAttachmentPath);
		sourceAttachmentPathText.setLayoutData(UIUtil.suggestCharWidth(UIUtil.stretchControlHorizontal(new GridData()), parent, 50));
		sourceAttachmentPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = sourceAttachmentPathText.getText();
				classPathEntry.sourceAttachmentPath = text.length() > 0 ? text : null;
			}
		});
		applyDialogFont(composite);
		return composite;
	}
}
