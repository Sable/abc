package org.jastadd.plugin.jastaddj.refactor.inlineTemp;

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
import AST.VariableDeclaration;

public class InlineTempRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;

	public InlineTempRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "Inline Temp";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof VariableDeclaration)
			/*OK*/;
		else
			status.addFatalError("Not a variable declaration.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		VariableDeclaration vd = (VariableDeclaration)selectedNode;

		Program root = vd.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			vd.doInline();

			changes = RefactoringUtil.createChanges("InlineTemp", Program.cloneUndoStack());
			
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
