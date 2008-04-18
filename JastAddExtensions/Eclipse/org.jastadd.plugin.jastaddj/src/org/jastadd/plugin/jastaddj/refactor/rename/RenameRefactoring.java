package org.jastadd.plugin.jastaddj.refactor.rename;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.model.JastAddModel;

import AST.ASTNode;
import AST.ChangeAccumulator;
import AST.RefactoringException;

public class RenameRefactoring extends Refactoring {

	private IEditorPart editorPart;
	private IFile editorFile;
	private JastAddModel model;
	private ISelection selection;
	private IJastAddNode selectedNode;
	private String name;
	private RefactoringStatus status;
	private Change changes;

	public RenameRefactoring(JastAddModel model, IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.model = model;
		this.editorPart = editorPart; {
		this.editorFile = editorFile;
		this.selection = selection;
			this.selectedNode = selectedNode;
		}
	}

	public String getName() {
		return "Rename";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if(status != null)
			return status;
		status = new RefactoringStatus();
		ASTNode n = (ASTNode)selectedNode;
		if(n != null) {
			try {
				n.rename(name);
				Stack ch = n.programRoot().cloneUndoStack();
				n.programRoot().undo();
				ChangeAccumulator accu = new ChangeAccumulator("Rename");
				accu.addAllEdits(model, ch.iterator());
				changes = accu.getChange();
			} catch (RefactoringException rfe) {
				status.addFatalError(rfe.getMessage());
				n.programRoot().undo();
			}
		}
		return status;
	}

	public void setName(String text) {
		name = text;
		status = null;
		changes = null;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return changes;
	}
}
