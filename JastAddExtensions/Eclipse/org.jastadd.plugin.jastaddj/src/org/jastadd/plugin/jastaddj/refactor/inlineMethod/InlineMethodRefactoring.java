package org.jastadd.plugin.jastaddj.refactor.inlineMethod;

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

public class InlineMethodRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private boolean delete = true;

	public InlineMethodRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "InlineMethod";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof MethodAccess || selectedNode instanceof MethodDecl)
			/*OK*/;
		else
			status.addFatalError("Can only inline method access or method declaration.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		if (selectedNode instanceof MethodDecl) {
			MethodDecl md = (MethodDecl)selectedNode;
			Program root = md.programRoot();
			try {
				pm.beginTask("Performing refactoring...", 1);
				
				RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
				
				Program.startRecordingASTChangesAndFlush();
			
				md.doInline(delete);

				changes = RefactoringUtil.createChanges("InlineMethod", Program.cloneUndoStack());
			} catch (RefactoringException re) {
				status.addFatalError(re.getMessage());
			} finally {
				Program.undoAll();
				root.flushCaches();
				pm.done();
			}
		} else { // selectedNode instanceof MethodAccess
			MethodAccess ma = (MethodAccess)selectedNode;
			Program root = ma.programRoot();
			try {
				pm.beginTask("Performing refactoring...", 1);
				
				RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
				
				Program.startRecordingASTChangesAndFlush();
			
				ma.doInline();
	
				changes = RefactoringUtil.createChanges("InlineMethod", Program.cloneUndoStack());
			} catch (RefactoringException re) {
				status.addFatalError(re.getMessage());
			} finally {
				Program.undoAll();
				root.flushCaches();
				pm.done();
			}
		}
		
		return status;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return changes;
	}

	public IJastAddNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(IJastAddNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
}
