package org.jastadd.plugin.jastaddj.refactor.encapsulateField;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class EncapsulateFieldWizard extends RefactoringWizard {
	public EncapsulateFieldWizard(EncapsulateFieldRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
	}
}
