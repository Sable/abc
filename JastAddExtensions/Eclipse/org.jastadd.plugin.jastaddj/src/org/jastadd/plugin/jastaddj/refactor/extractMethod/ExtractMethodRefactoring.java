package org.jastadd.plugin.jastaddj.refactor.extractMethod;

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

import AST.Block;
import AST.Program;
import AST.RefactoringException;

public class ExtractMethodRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private String methodName = "ExtractedMethod";
	private int visibility = 0;
	private int begin = 0;
	private int end = 0;

	public ExtractMethodRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.selectedNode = selectedNode;
	}

	public String getName() {
		return "ExtractMethod";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof Block && ((Block) selectedNode).getNumStmt() > 0)
			/*OK*/;
		else
			status.addFatalError("Can only extract from a block with at least one statement.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();
		Block block = (Block)selectedNode;
		Program root = block.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			block.doExtractMethod(visibility, methodName, begin, end);

			changes = RefactoringUtil.createChanges("ExtractMethod", Program.cloneUndoStack());
		} catch (RefactoringException re) {
			status.addFatalError(re.getMessage());
		} finally {
			Program.undoAll();
			root.flushCaches();
			pm.done();
		}
		return status;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if(changes == null)
			throw new Error("changes should have been created");
		return changes;
	}

	public IJastAddNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(IJastAddNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
