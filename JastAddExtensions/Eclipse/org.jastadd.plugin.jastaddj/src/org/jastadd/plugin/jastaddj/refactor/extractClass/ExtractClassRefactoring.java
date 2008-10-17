package org.jastadd.plugin.jastaddj.refactor.extractClass;

import java.util.ArrayList;
import java.util.Collection;
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

import AST.ASTChange;
import AST.ChangeAccumulator;
import AST.ClassDecl;
import AST.FieldDeclaration;
import AST.RefactoringException;

public class ExtractClassRefactoring extends Refactoring {

	private JastAddModel model;
	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private ArrayList<FieldDeclaration> fds;
	private String newClassName;
	private String newFieldName;

	public ExtractClassRefactoring(JastAddModel model, IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.model = model;
		this.selectedNode = selectedNode;
		this.fds = new ArrayList<FieldDeclaration>();
		if(selectedNode instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)selectedNode;
			for(FieldDeclaration fd : 
				(Collection<FieldDeclaration>)cd.localFieldsMap().values())
				fds.add(fd);
		}
	}

	public String getName() {
		return "ExtractClass";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(selectedNode instanceof ClassDecl)
			/*OK*/;
		else
			status.addFatalError("Can only extract from classes.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if(status != null)
			return status;
		status = new RefactoringStatus();
		ClassDecl cd = (ClassDecl)selectedNode;
		try {
			cd.extractClass(getFields(), newClassName, newFieldName);
			Stack<ASTChange> ch = cd.programRoot().cloneUndoStack();
			cd.programRoot().undo();
			ChangeAccumulator accu = new ChangeAccumulator("ExtractClass");
			accu.addAllEdits(model, ch.iterator());
			changes = accu.getChange();
		} catch (RefactoringException rfe) {
			status.addFatalError(rfe.getMessage());
			cd.programRoot().undo();
			changes = null;
		}
		return status;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return changes;
	}

	public void setClassName(String name) {
		newClassName = name;
	}

	public void addField(FieldDeclaration fd) {
		fds.add(fd);
	}

	public void removeField(FieldDeclaration fd) {
		fds.remove(fd);
	}

	public FieldDeclaration[] getFields() {
		return fds.toArray(new FieldDeclaration[]{});
	}

	public void setFieldName(String name) {
		newFieldName = name;
	}
}
