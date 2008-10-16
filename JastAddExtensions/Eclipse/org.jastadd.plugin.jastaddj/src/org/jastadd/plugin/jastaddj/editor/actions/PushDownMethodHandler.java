package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.refactor.pushDownMethod.PushDownMethodRefactoring;
import org.jastadd.plugin.jastaddj.refactor.pushDownMethod.PushDownMethodWizard;
import org.jastadd.plugin.refactor.RefactoringSaveHelper;

public class PushDownMethodHandler extends JastAddActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			PushDownMethodRefactoring refactoring = new 
				PushDownMethodRefactoring(this.activeModel(), 
										  this.activeEditorPart(), 
										  this.activeEditorFile(), 
										  this.activeSelection(), 
										  this.selectedNode());
			PushDownMethodWizard wizard = 
				new PushDownMethodWizard(refactoring, "PushDownMethod");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"PushDownMethod");
		} catch (InterruptedException e) {
		}
	}
}
