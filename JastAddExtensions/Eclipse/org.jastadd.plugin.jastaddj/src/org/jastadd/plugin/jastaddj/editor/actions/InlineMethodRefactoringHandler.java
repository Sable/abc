package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.inlineMethod.InlineMethodRefactoring;
import org.jastadd.plugin.jastaddj.refactor.inlineMethod.InlineMethodWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class InlineMethodRefactoringHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			InlineMethodRefactoring refactoring = new 
				InlineMethodRefactoring(this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			InlineMethodWizard wizard = 
				new InlineMethodWizard(refactoring, "InlineMethod");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"InlineMethod");
		} catch (InterruptedException e) {
		}
	}

}
