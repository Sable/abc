package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.refactor.rename.*;
import org.jastadd.plugin.refactor.RefactoringSaveHelper;

public class RenameRefactoringHandler extends JastAddActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			RenameRefactoring refactoring = new RenameRefactoring(this
					.activeModel(), this.activeEditorPart(), this
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
