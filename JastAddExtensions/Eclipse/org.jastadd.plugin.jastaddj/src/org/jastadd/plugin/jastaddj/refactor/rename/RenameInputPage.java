package org.jastadd.plugin.jastaddj.refactor.rename;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RenameInputPage extends UserInputWizardPage {
	public RenameInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);

		Label label = new Label(result, SWT.NONE);
		label.setText("Name:");
		final Text name = new Text(result, SWT.SINGLE);
		name.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				((RenameRefactoring)getRefactoring()).setName(name.getText());
			}
			
		});
	}
}
