package org.jastadd.plugin.jastaddj.refactor.extractClass;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ExtractClassWizard extends RefactoringWizard {
	public ExtractClassWizard(ExtractClassRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		ExtractClassInputPage page= new ExtractClassInputPage("ExtractClass");
		addPage(page);
	}
}
