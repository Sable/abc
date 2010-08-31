package org.jastadd.plugin.jastaddj.refactor.rename;

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
import org.jastadd.plugin.jastaddj.AST.IJastAddJRenameConditionNode;
import org.jastadd.plugin.jastaddj.refactor.RefactoringUtil;

import AST.ASTNode;
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;
import AST.Variable;

public class RenameRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private String name;
	private RefactoringStatus status;
	private Change changes;

	public RenameRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "Rename";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof TypeDecl
				|| selectedNode instanceof MethodDecl
				|| selectedNode instanceof Variable)
			/*OK*/;
		else
			status.addFatalError("Only types, methods, and variables can be renamed.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		status = new RefactoringStatus();
		if(selectedNode instanceof IJastAddJRenameConditionNode)
			changes = ((IJastAddJRenameConditionNode)selectedNode).checkRenameConditions(name, status);
		
		Program root = ((ASTNode) selectedNode).programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
			
			if (selectedNode instanceof Variable) {
				((Variable) selectedNode).rename(name);
			} else if (selectedNode instanceof MethodDecl) {
				((MethodDecl) selectedNode).rename(name);
			} else if (selectedNode instanceof TypeDecl) {
				((TypeDecl) selectedNode).rename(name);
			}
			
			changes = RefactoringUtil.createChanges("Rename", Program.cloneUndoStack());
		} catch (RefactoringException re) {
			status.addFatalError(re.getMessage());
		} finally {
			Program.undoAll();
			root.flushCaches();
			pm.done();
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
