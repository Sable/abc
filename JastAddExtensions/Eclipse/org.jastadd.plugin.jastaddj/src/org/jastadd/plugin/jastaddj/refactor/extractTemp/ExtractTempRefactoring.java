package org.jastadd.plugin.jastaddj.refactor.extractTemp;

import java.util.Collection;

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

import AST.ClassDecl;
import AST.Expr;
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;

public class ExtractTempRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private String varName = "temp";
	private boolean makeFinal = false;

	public ExtractTempRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "ExtractTemp";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof Expr)
			/*OK*/;
		else
			status.addFatalError("Can only extract expressions.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		Expr expr = (Expr)selectedNode;
		Program root = expr.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			expr.doExtract(varName, makeFinal);

			changes = RefactoringUtil.createChanges("ExtractTemp", Program.cloneUndoStack());
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

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public boolean isMakeFinal() {
		return makeFinal;
	}

	public void setMakeFinal(boolean makeFinal) {
		this.makeFinal = makeFinal;
	}
}
