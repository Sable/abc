package org.jastadd.plugin.jastaddj.refactor.pullUpMethod;

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

import AST.Expr;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;

public class PullUpMethodRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private boolean onlyAbstract = false;

	public PullUpMethodRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "PullUpMethod";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof MethodDecl)
			/*OK*/;
		else
			status.addFatalError("Can only pull up a method declaration.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		MethodDecl md = (MethodDecl)selectedNode;
		Program root = md.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			md.doPullUp(onlyAbstract);

			changes = RefactoringUtil.createChanges("PullUpMethod", Program.cloneUndoStack());
			
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

	public boolean isOnlyAbstract() {
		return onlyAbstract;
	}

	public void setOnlyAbstract(boolean onlyAbstract) {
		this.onlyAbstract = onlyAbstract;
	}
}
