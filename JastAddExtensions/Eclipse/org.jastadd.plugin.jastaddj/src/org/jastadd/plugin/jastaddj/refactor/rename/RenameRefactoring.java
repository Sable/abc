package org.jastadd.plugin.jastaddj.refactor.rename;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorPart;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.model.JastAddModel;

import AST.ASTChange;
import AST.ASTNode;
import AST.ClassDecl;
import AST.ConstructorDecl;
import AST.FieldDeclaration;
import AST.InterfaceDecl;
import AST.MethodDecl;
import AST.Named;
import AST.ParameterDeclaration;
import AST.RefactoringException;
import AST.Rename;
import AST.ReplaceNode;
import AST.TypeDecl;
import AST.VariableDeclaration;

public class RenameRefactoring extends Refactoring {
	private IEditorPart editorPart;

	private IFile editorFile;

	private JastAddModel model;

	private ISelection selection;

	private IJastAddNode selectedNode;

	private String name;

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
		return new RefactoringStatus();
	}

	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			if(selectedNode instanceof VariableDeclaration) {
				VariableDeclaration v = (VariableDeclaration)selectedNode;
				v.rename(name);
				Iterator iter = v.programRoot().getUndoIterator();
				CompositeChange changes = createEdits(iter);
				v.programRoot().undo();
				return changes;
			}
			else if(selectedNode instanceof ParameterDeclaration) {
				ParameterDeclaration v = (ParameterDeclaration)selectedNode;
				v.rename(name);
				Iterator iter = v.programRoot().getUndoIterator();
				CompositeChange changes = createEdits(iter);
				v.programRoot().undo();
				return changes;
			}
			else if(selectedNode instanceof FieldDeclaration) {
				FieldDeclaration f = (FieldDeclaration)selectedNode;
				f.rename(name);
				CompositeChange changes = createEdits(f.programRoot().getUndoIterator());
				f.programRoot().undo();
				return changes;
			}
			else if(selectedNode instanceof MethodDecl) {
				MethodDecl f = (MethodDecl)selectedNode;
				f.rename(name);
				CompositeChange changes = createEdits(f.programRoot().getUndoIterator());
				f.programRoot().undo();
				return changes;
			}
			else if(selectedNode instanceof ClassDecl || selectedNode instanceof InterfaceDecl) {
				TypeDecl f = (ClassDecl)selectedNode;
				f.rename(name);
				CompositeChange changes = createEdits(f.programRoot().getUndoIterator());
				f.programRoot().undo();
				return changes;
			}
		} catch (RefactoringException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new OperationCanceledException("Could not perform rename"); 
	}

	private void createEdit(CompositeChange changes, Map<IFile, TextFileChange> map, IFile file, int offset, int length, String contents) {
		TextFileChange tfc;
		if(!map.containsKey(file)) {
			tfc = new TextFileChange("Rename", file);
			tfc.setEdit(new MultiTextEdit());
			changes.add(tfc);
			map.put(file, tfc);
		}
		else {
			tfc = map.get(file);
		}
		tfc.addEdit(new ReplaceEdit(offset, length, contents));
	}
	
	private CompositeChange createEdits(Iterator iter) {
		Map<IFile, TextFileChange> map = new HashMap<IFile, TextFileChange>();
		CompositeChange changes = new CompositeChange("Rename");
		while(iter.hasNext()) {
			ASTChange change = (ASTChange)iter.next();
			if(change instanceof ReplaceNode) {
				ReplaceNode r = (ReplaceNode)change;
				ASTNode before = r.getBefore();
				ASTNode after = r.getAfter();
				IFile file = model.getFile(after);
				int offset = before.getBeginOffset();
				int length = before.getEndOffset() - offset + 1;
				String contents = after.toString();
				createEdit(changes, map, file, offset, length, contents);
			}
			if(change instanceof Rename) {
				Rename r = (Rename)change;
				Named e = r.getEntity();
				if(e instanceof VariableDeclaration) {
					VariableDeclaration a = (VariableDeclaration)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
				else if(e instanceof ParameterDeclaration) {
					ParameterDeclaration a = (ParameterDeclaration)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
				else if(e instanceof FieldDeclaration) {
					FieldDeclaration a = (FieldDeclaration)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
				else if(e instanceof MethodDecl) {
					MethodDecl a = (MethodDecl)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
				else if(e instanceof ConstructorDecl) {
					ConstructorDecl a = (ConstructorDecl)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
				else if(e instanceof TypeDecl) {
					TypeDecl a = (TypeDecl)e;
					IFile file = model.getFile(a);
					int offset = a.createOffset(a.IDstart);
					int offsetEnd = a.createOffset(a.IDend);
					int length = offsetEnd - offset + 1;
					String contents = name;
					createEdit(changes, map, file, offset, length, contents);
				}
			}
		}
		return changes;
	}

	public void setName(String text) {
		name = text;
	}
}
