package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.extractMethod.ExtractMethodRefactoring;
import org.jastadd.plugin.jastaddj.refactor.extractMethod.ExtractMethodWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class ExtractMethodRefactoringHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			ExtractMethodRefactoring refactoring = new 
				ExtractMethodRefactoring(this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			ExtractMethodWizard wizard = 
				new ExtractMethodWizard(refactoring, "ExtractMethod");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"ExtractMethod");
		} catch (InterruptedException e) {
		}
	}

}
