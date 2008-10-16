package org.jastadd.plugin.jastaddj.refactor.pushDownMethod;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class PushDownMethodWizard extends RefactoringWizard {
	public PushDownMethodWizard(PushDownMethodRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
	}
}
