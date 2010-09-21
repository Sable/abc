package org.jastadd.plugin.jastaddj.refactor.addParameter;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddParameterInputPage extends UserInputWizardPage {
	public AddParameterInputPage(String name) {
		super(name);
	}
	
	public void createControl(Composite parent) {
		final AddParameterRefactoring refactoring = (AddParameterRefactoring)getRefactoring();
		
		refactoring.setName("newParam");
		refactoring.setType("java.lang.Object");
		
		initializeDialogUnits(parent);
		Composite result = new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout(2, false));
		
		createLabelAndText(result, "Parameter name:", "newParam", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setName(text);
			}
		});
		
		createLabelAndText(result, "Parameter type:", "java.lang.Object", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setType(text);
			}
		});
		
		createLabelAndText(result, "Default value:", "null", new TextModificationListener() {
			public void modified(String text) {
				refactoring.setDefaultValue(text);
			}
		});

		setControl(result);
	}
	
	private abstract class TextModificationListener {
		public abstract void modified(String text);
	}
	
	private void createLabelAndText(Composite result, String labelText, String deflt, final TextModificationListener listener) {
		Label label = new Label(result, SWT.NONE);
		label.setText(labelText);
		
		final Text textInput = new Text(result, SWT.BORDER);
		textInput.setText(deflt);
		textInput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				listener.modified(textInput.getText());
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(25);
		textInput.setLayoutData(gd);
	}
}
