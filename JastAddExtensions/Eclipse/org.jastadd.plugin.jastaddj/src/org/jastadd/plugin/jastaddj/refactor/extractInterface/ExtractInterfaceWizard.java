package org.jastadd.plugin.jastaddj.refactor.extractInterface;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ExtractInterfaceWizard extends RefactoringWizard {
	public ExtractInterfaceWizard(ExtractInterfaceRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		ExtractInterfaceInputPage page= new ExtractInterfaceInputPage("ExtractInterface");
		addPage(page);
	}
}
