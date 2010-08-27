package org.jastadd.plugin.jastaddj.refactor.inlineMethod;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import AST.MethodDecl;

public class InlineMethodWizard extends RefactoringWizard {
	InlineMethodRefactoring refactoring;
	public InlineMethodWizard(InlineMethodRefactoring refactoring, String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		this.refactoring = refactoring;
		setDefaultPageTitle(pageTitle);
	}

	protected void addUserInputPages() {
		if (refactoring.getSelectedNode() instanceof MethodDecl) {
			InlineMethodDeclInputPage page= new InlineMethodDeclInputPage("InlineMethod");
			addPage(page);
		}
	}
}
