package org.jastadd.plugin.jastaddj.refactor.changeParameterType;

import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.util.RowLayouter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jastadd.plugin.jastaddj.refactor.rename.RenameRefactoring;

import AST.ASTNode;

public class ChangeParameterTypeInputPage extends UserInputWizardPage {
	private String initialSuggestion;
	private Text text;
	
	public ChangeParameterTypeInputPage(String name) {
		super(name);
		initialSuggestion = "";
	}
	
	// see RenameInputPage
	public void createControl(Composite parent) {
		Composite ctrl= new Composite(parent, SWT.NONE);
		setControl(ctrl);
		initializeDialogUnits(ctrl);
		ctrl.setLayout(new GridLayout());
		Composite composite= new Composite(ctrl, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;

		composite.setLayout(layout);
		RowLayouter layouter= new RowLayouter(2);
		
		Label label= new Label(composite, SWT.NONE);
		label.setText("New type:");
		
		Text text= createTextInputField(composite);
		text.selectAll();
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(25);
		text.setLayoutData(gd);

		layouter.perform(label, text, 1);

		Label separator= new Label(composite, SWT.NONE);
		GridData gridData= new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.heightHint= 2;
		separator.setLayoutData(gridData);
		
		
		int indent= convertWidthInCharsToPixels(2);
		
		Dialog.applyDialogFont(ctrl);
	}
	
	protected Text createTextInputField(Composite parent) {
		return createTextInputField(parent, SWT.BORDER);
	}
	
	protected Text createTextInputField(Composite parent, int style) {
		text= new Text(parent, style);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textModified(getText());
			}
		});
		text.setText(initialSuggestion);
		TextFieldNavigationHandler.install(text);
		return text;
	}
	
	protected String getText() {
		if (text == null)
			return null;
		return text.getText();	
	}
	
	protected void textModified(String text) {	
		if("".equals(text)) {
			setPageComplete(false);
			setErrorMessage(null);
			restoreMessage();
			return;
		}
		((ChangeParameterTypeRefactoring)getRefactoring()).setType(text);
		setPageComplete(new RefactoringStatus());
	}

	private void restoreMessage() {
		setMessage(null);
	}	
	
}
