package org.jastadd.plugin.jastaddj.refactor.encapsulateField;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.refactor.RefactoringUtil;

import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;

public class EncapsulateFieldRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;

	public EncapsulateFieldRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "Encapsulate Field";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof FieldDeclaration)
			/*OK*/;
		else
			status.addFatalError("Not a field.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		FieldDeclaration fd = (FieldDeclaration)selectedNode;

		Program root = fd.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			fd.doSelfEncapsulate();

			changes = RefactoringUtil.createChanges("EncapsulateField", Program.cloneUndoStack());
		} catch (RefactoringException re) {
			status.addFatalError(re.getMessage());
		} finally {
			Program.undoAll();
			root.flushCaches();
			pm.done();
		}
		return status;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return changes;
	}
}
