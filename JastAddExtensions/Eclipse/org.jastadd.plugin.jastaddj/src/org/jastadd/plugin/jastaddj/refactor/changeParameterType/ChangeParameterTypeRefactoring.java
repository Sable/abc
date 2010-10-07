package org.jastadd.plugin.jastaddj.refactor.changeParameterType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.refactor.RefactoringUtil;

import AST.ASTNode;
import AST.BodyDecl;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;

public class ChangeParameterTypeRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private String newType;
	private RefactoringStatus status;
	private Change changes;

	public ChangeParameterTypeRefactoring(IJastAddNode selectedNode) {
		super();
		if(selectedNode instanceof ParameterDeclaration) {
			this.selectedNode = selectedNode;
		} else {
			this.selectedNode = null;
		}
	}

	public String getName() {
		return "ChangeParameterTypeRefactoring";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof ParameterDeclaration)
			/*OK*/;
		else
			status.addFatalError("Please select a parameter.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		ParameterDeclaration pd = (ParameterDeclaration)selectedNode;
		Program root = ((ASTNode)pd).programRoot();
		
		TypeDecl td = root.findType(newType);
		if(td == null) {
			status.addFatalError("Couldn't find type " + newType);
			return status;
		}
		BodyDecl bd = pd.enclosingBodyDecl();
		if(!(bd instanceof MethodDecl)) {
			status.addFatalError("Not a method parameter");
		}
		
		try {
			pm.beginTask("Performing refactoring...", 1);

			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			Program.startRecordingASTChangesAndFlush();

			pd.changeType(td);

			changes = RefactoringUtil.createChanges("ChangeParameterType", Program.cloneUndoStack());
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

	public void setType(String newType) {
		this.newType = newType;
	}
}
