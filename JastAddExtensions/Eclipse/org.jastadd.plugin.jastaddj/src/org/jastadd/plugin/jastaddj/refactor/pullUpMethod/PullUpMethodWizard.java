package org.jastadd.plugin.jastaddj.refactor.pullUpMethod;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import AST.MethodDecl;

public class PullUpMethodWizard extends RefactoringWizard {
	PullUpMethodRefactoring refactoring;
	public PullUpMethodWizard(PullUpMethodRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		this.refactoring = refactoring;
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		PullUpMethodInputPage page= new PullUpMethodInputPage("PullUpMethod");
		addPage(page);
	}
}
