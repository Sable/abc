package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.pullUpMethod.PullUpMethodRefactoring;
import org.jastadd.plugin.jastaddj.refactor.pullUpMethod.PullUpMethodWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class PullUpMethodRefactoringHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			PullUpMethodRefactoring refactoring = new 
				PullUpMethodRefactoring(this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			PullUpMethodWizard wizard = 
				new PullUpMethodWizard(refactoring, "PullUpMethod");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"PullUpMethod");
		} catch (InterruptedException e) {
		}
	}

}
