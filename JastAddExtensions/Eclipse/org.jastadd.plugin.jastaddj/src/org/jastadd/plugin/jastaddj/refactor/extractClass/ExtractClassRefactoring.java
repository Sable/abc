package org.jastadd.plugin.jastaddj.refactor.extractClass;

import java.util.ArrayList;
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
import AST.FieldDeclaration;
import AST.Program;
import AST.RefactoringException;

public class ExtractClassRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private RefactoringStatus status;
	private Change changes;
	private ArrayList<FieldDeclaration> fds;
	private String newClassName = "newClassName";
	private String newFieldName = "newFieldName";
	private boolean encapsulate = true;
	private boolean topLevel = false;

	public ExtractClassRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
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
		return status;
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		ClassDecl cd = (ClassDecl)selectedNode;
		Program root = cd.programRoot();
		try {
			pm.beginTask("Creating change...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			
			Program.startRecordingASTChangesAndFlush();
		
			cd.doExtractClass(getFields(), newClassName, newFieldName, encapsulate, topLevel);

			changes = RefactoringUtil.createChanges("ExtractClass", Program.cloneUndoStack());
			
//			Stack<ASTChange> ch = cd.programRoot().cloneUndoStack();
//			// need to do some pre-processing here; UGLY!!!
//			Iterator<ASTChange> chiter = ch.iterator();
//			InsertBodyDecl insertClassChange = null;
//			ClassDecl memberClass = null;
//			InsertBodyDecl insertFieldChange = null;
//			FieldDeclaration memberField = null;
//			while(chiter.hasNext()) {
//				ASTChange ach = chiter.next();
//				if(ach instanceof ChangeFieldModifiers) {
//					ChangeFieldModifiers fmch = (ChangeFieldModifiers)ach;
//					FieldDeclaration fd = fmch.getFieldDeclaration();
//					if(fd == memberField || fd.hostType() == memberClass)
//						chiter.remove();
//				} else if(ach instanceof InsertBodyDecl) {
//					InsertBodyDecl ibch = (InsertBodyDecl)ach;
//					BodyDecl bd = ibch.getBodyDecl();
//					if(bd instanceof MemberClassDecl) {
//						insertClassChange = ibch;
//						memberClass = ((MemberClassDecl)insertClassChange.getBodyDecl()).getClassDecl();
//					} else if(bd instanceof FieldDeclaration) {
//						insertFieldChange = ibch;
//						memberField = (FieldDeclaration)(insertFieldChange).getBodyDecl();
//					} else if(bd.hostType() == memberClass) {
//						chiter.remove();
//					}
//				}
//			}
//			insertClassChange.updateText();
//			insertFieldChange.updateText();
//			cd.programRoot().undo();
//			ChangeAccumulator accu = new ChangeAccumulator("ExtractClass");
//			accu.addAllEdits(ch.iterator());
//			changes = accu.getChange();
//		} catch (RefactoringException rfe) {
//			status.addFatalError(rfe.getMessage());
//			cd.programRoot().undo();
//			changes = null;
			return changes;
		} catch (RefactoringException re) {
			throw re;
		} finally {
			Program.undoAll();
			root.flushCaches();
			pm.done();
		}
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

	public Collection<FieldDeclaration> getFields() {
		return fds;
	}

	public void setFieldName(String name) {
		newFieldName = name;
	}

	public void setEncapsulate(boolean encapsulate) {
		this.encapsulate = encapsulate;
	}

	public void setTopLevel(boolean topLevel) {
		this.topLevel = topLevel;
	}

	public String getClassName() {
		return newClassName;
	}

	public String getFieldName() {
		return newFieldName;
	}

	public boolean getEncapsulate() {
		return encapsulate;
	}

	public boolean getTopLevel() {
		return topLevel;
	}
}
