package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.extractInterface.ExtractInterfaceRefactoring;
import org.jastadd.plugin.jastaddj.refactor.extractInterface.ExtractInterfaceWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class ExtractInterfaceRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			ExtractInterfaceRefactoring refactoring = new ExtractInterfaceRefactoring(
					this.activeEditorPart(), this
					.activeEditorFile(), this.activeSelection(), this
					.selectedNode());
			ExtractInterfaceWizard wizard = new ExtractInterfaceWizard(refactoring,
					"ExtractInterface");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
					wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"ExtractInterface");
		} catch (InterruptedException e) {
		}
	}
}
