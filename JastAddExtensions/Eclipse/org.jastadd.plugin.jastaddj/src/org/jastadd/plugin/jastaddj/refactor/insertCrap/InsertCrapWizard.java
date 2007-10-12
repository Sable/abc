/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.jastadd.plugin.jastaddj.refactor.insertCrap;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class InsertCrapWizard extends RefactoringWizard {
    public InsertCrapWizard(InsertCrapRefactoring refactoring, String pageTitle) {
	super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
	setDefaultPageTitle(pageTitle);
    }

    protected void addUserInputPages() {
	InsertCrapInputPage page= new InsertCrapInputPage("Insert Crap");

	addPage(page);
    }
}
