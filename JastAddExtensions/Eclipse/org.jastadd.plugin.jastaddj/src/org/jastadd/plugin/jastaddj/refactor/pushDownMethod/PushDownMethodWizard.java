package org.jastadd.plugin.jastaddj.refactor.pushDownMethod;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import AST.MethodDecl;

public class PushDownMethodWizard extends RefactoringWizard {
	PushDownMethodRefactoring refactoring;
	public PushDownMethodWizard(PushDownMethodRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		this.refactoring = refactoring;
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		PushDownMethodInputPage page= new PushDownMethodInputPage("PushDownMethod");
		addPage(page);
	}
}
