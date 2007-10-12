/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.jastadd.plugin.jastaddj.refactor.insertCrap;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class InsertCrapInputPage extends UserInputWizardPage {
	public InsertCrapInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		result.setLayout(layout);

		final Button enhancedButton = new Button(result, SWT.CHECK);

		enhancedButton.setText("Enhanced");
		enhancedButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		enhancedButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				((InsertCrapRefactoring)getRefactoring()).setEnhanced(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}
