/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.jastadd.plugin.jastaddj.refactor.insertCrap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.compiler.ast.IJastAddNode;

public class InsertCrapRefactoring extends Refactoring {
	private IEditorPart editorPart;

	private IFile editorFile;

	private ISelection selection;

	private IJastAddNode selectedNode;

	private boolean isEnhanced;

	public InsertCrapRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.editorPart = editorPart; {
		this.editorFile = editorFile; // aaaa
		this.selection = selection;
			this.selectedNode = selectedNode;
		}
	}

	public String getName() {
		return "Insert Crap";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		TextFileChange tfc = new TextFileChange("Insert Crap", editorFile);

		tfc.setEdit(new MultiTextEdit());

		tfc.addEdit(new ReplaceEdit(((ITextSelection) selection).getOffset(),
				((ITextSelection) selection).getLength(),
				isEnhanced ? "<Inserted ENHANCED Crap as Ordered!>"
						: "<Inserted Crap as Ordered!>"));

		return tfc;
	}

	public void setEnhanced(boolean enhanced) {
		this.isEnhanced = enhanced;
	}
}
