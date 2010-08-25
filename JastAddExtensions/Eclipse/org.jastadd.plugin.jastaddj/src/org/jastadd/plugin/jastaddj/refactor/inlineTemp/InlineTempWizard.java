package org.jastadd.plugin.jastaddj.refactor.inlineTemp;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class InlineTempWizard extends RefactoringWizard {
	public InlineTempWizard(InlineTempRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
	}
}
