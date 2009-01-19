package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.insertCrap.InsertCrapRefactoring;
import org.jastadd.plugin.jastaddj.refactor.insertCrap.InsertCrapWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class InsertCrapRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			InsertCrapRefactoring refactoring = new InsertCrapRefactoring(
					this.activeEditorPart(), this
					.activeEditorFile(), this.activeSelection(), this
					.selectedNode());
			InsertCrapWizard wizard = new InsertCrapWizard(refactoring,
					"Insert Crap");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"Insert Crap");
		} catch (InterruptedException e) {
		}
	}
}
