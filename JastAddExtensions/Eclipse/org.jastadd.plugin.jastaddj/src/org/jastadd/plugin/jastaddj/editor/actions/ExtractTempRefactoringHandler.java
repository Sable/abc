package org.jastadd.plugin.jastaddj.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.jastadd.plugin.jastaddj.refactor.extractTemp.ExtractTempRefactoring;
import org.jastadd.plugin.jastaddj.refactor.extractTemp.ExtractTempWizard;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;
import org.jastadd.plugin.util.RefactoringSaveHelper;

public class ExtractTempRefactoringHandler extends AbstractBaseActionDelegate {

	@Override
	public void run(IAction action) {
		try {
			if (!RefactoringSaveHelper.makeSureEditorsSaved(
					this.activeEditorPart().getSite().getShell()))
				return;

			this.selectedNode();
			ExtractTempRefactoring refactoring = new 
				ExtractTempRefactoring(this.activeEditorPart(), 
						this.activeEditorFile(), 
						this.activeSelection(), 
						this.selectedNode());
			ExtractTempWizard wizard = 
				new ExtractTempWizard(refactoring, "ExtractTemp");
			RefactoringWizardOpenOperation op = 
				new RefactoringWizardOpenOperation(wizard);
			op.run(this.activeEditorPart().getSite().getShell(),
					"ExtractTemp");
		} catch (InterruptedException e) {
		}
	}

}
