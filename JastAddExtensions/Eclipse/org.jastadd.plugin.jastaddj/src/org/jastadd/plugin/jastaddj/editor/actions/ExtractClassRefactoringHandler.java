package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.extractClass.*;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class ExtractClassRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			ExtractClassRefactoring refactoring = new ExtractClassRefactoring(
					this.activeEditorPart(), this
					.activeEditorFile(), this.activeSelection(), this
					.selectedNode());
			ExtractClassWizard wizard = new ExtractClassWizard(refactoring,
					"ExtractClass");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"ExtractClass");
		} catch (InterruptedException e) {
		}
	}
}
