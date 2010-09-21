package org.jastadd.plugin.jastaddj.refactor.addParameter;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class AddParameterWizard extends RefactoringWizard {
	public AddParameterWizard(AddParameterRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | CHECK_INITIAL_CONDITIONS_ON_OPEN | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		AddParameterInputPage page = new AddParameterInputPage("AddParameter");
		addPage(page);
	}
}
