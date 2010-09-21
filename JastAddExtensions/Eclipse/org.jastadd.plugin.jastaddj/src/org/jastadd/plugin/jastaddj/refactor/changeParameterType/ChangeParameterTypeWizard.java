package org.jastadd.plugin.jastaddj.refactor.changeParameterType;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ChangeParameterTypeWizard extends RefactoringWizard {
	public ChangeParameterTypeWizard(ChangeParameterTypeRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | CHECK_INITIAL_CONDITIONS_ON_OPEN | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		ChangeParameterTypeInputPage page = new ChangeParameterTypeInputPage("ChangeParameterType");
		addPage(page);
	}
}
