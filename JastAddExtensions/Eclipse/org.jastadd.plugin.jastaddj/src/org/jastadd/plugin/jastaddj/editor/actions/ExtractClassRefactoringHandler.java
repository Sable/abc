package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.refactor.extractClass.*;
import org.jastadd.plugin.refactor.RefactoringSaveHelper;

public class ExtractClassRefactoringHandler extends JastAddActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			ExtractClassRefactoring refactoring = new ExtractClassRefactoring(this
					.activeModel(), this.activeEditorPart(), this
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
