package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.changeAccessibility.ChangeAccessibilityWizard;
import org.jastadd.plugin.jastaddj.refactor.changeParameterType.ChangeParameterTypeRefactoring;
import org.jastadd.plugin.jastaddj.refactor.changeParameterType.ChangeParameterTypeWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class ChangeParameterTypeRefactoringHandler extends AbstractBaseActionDelegate {
	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			ChangeParameterTypeRefactoring refactoring = new ChangeParameterTypeRefactoring(this.selectedNode());
			ChangeParameterTypeWizard wizard = new ChangeParameterTypeWizard(refactoring, "ChangeParameterType");
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(), "ChangeParameterType");
		} catch (InterruptedException e) {
		}
	}
}
