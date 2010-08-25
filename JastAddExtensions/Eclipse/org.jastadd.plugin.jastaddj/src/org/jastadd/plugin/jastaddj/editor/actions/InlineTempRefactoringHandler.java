package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.inlineTemp.InlineTempRefactoring;
import org.jastadd.plugin.jastaddj.refactor.inlineTemp.InlineTempWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class InlineTempRefactoringHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			InlineTempRefactoring refactoring = new 
				InlineTempRefactoring(this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			InlineTempWizard wizard = 
				new InlineTempWizard(refactoring, "InlineTemp");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"InlineTemp");
		} catch (InterruptedException e) {
		}
	}

}
