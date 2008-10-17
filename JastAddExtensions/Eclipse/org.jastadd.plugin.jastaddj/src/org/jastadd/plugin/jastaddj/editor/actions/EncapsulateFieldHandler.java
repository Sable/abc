package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.editor.actions.JastAddActionDelegate;
import org.jastadd.plugin.jastaddj.refactor.encapsulateField.EncapsulateFieldRefactoring;
import org.jastadd.plugin.jastaddj.refactor.encapsulateField.EncapsulateFieldWizard;
import org.jastadd.plugin.refactor.RefactoringSaveHelper;

public class EncapsulateFieldHandler extends JastAddActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			EncapsulateFieldRefactoring refactoring = new 
				EncapsulateFieldRefactoring(this.activeModel(), 
						this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			EncapsulateFieldWizard wizard = 
				new EncapsulateFieldWizard(refactoring, "EncapsulateField");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"EncapsulateField");
		} catch (InterruptedException e) {
		}
	}
}
