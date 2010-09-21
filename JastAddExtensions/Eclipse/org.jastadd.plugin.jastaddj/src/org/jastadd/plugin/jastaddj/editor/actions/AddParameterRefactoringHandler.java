package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.addParameter.AddParameterRefactoring;
import org.jastadd.plugin.jastaddj.refactor.addParameter.AddParameterWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class AddParameterRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			AddParameterRefactoring refactoring = new AddParameterRefactoring(this.selectedNode());
			AddParameterWizard wizard = new AddParameterWizard(refactoring, "AddParameter");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(), "AddParameter");
		} catch (InterruptedException e) {
		}
	}
}
