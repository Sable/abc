package org.jastadd.plugin.jastaddj.refactor.extractTemp;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ExtractTempWizard extends RefactoringWizard {
	public ExtractTempWizard(ExtractTempRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		ExtractTempInputPage page= new ExtractTempInputPage("ExtractTemp");
		addPage(page);
	}
}
