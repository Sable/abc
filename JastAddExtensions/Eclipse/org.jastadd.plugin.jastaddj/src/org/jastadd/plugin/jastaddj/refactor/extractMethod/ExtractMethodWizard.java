package org.jastadd.plugin.jastaddj.refactor.extractMethod;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ExtractMethodWizard extends RefactoringWizard {
	public ExtractMethodWizard(ExtractMethodRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		ExtractMethodInputPage page= new ExtractMethodInputPage("ExtractTemp");
		addPage(page);
	}
}
