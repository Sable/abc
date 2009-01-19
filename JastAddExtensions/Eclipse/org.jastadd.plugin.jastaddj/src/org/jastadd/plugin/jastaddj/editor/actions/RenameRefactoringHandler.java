package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.rename.*;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class RenameRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			RenameRefactoring refactoring = new RenameRefactoring(this.activeEditorPart(), this
					.activeEditorFile(), this.activeSelection(), this
					.selectedNode());
			RenameWizard wizard = new RenameWizard(refactoring,
					"Rename");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"Rename");
		} catch (InterruptedException e) {
		}
	}
}
