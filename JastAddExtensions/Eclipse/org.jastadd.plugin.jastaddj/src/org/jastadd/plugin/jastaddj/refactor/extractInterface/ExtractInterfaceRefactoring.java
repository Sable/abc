package org.jastadd.plugin.jastaddj.refactor.extractInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
import AST.MethodDecl;
import AST.Program;
import AST.RefactoringException;

public class ExtractInterfaceRefactoring extends Refactoring {

	private ClassDecl cd;
	private RefactoringStatus status;
	private Change changes;
	private ArrayList<MethodDecl> mds;
	private String pkgName = "p";
	private String ifaceName = "I";

	public ExtractInterfaceRefactoring(IEditorPart editorPart,
			IFile editorFile, ISelection selection, IJastAddNode selectedNode) {
		super();
		this.mds = new ArrayList<MethodDecl>();
		if(selectedNode instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl)selectedNode;
			this.cd = cd;
			for(Iterator<MethodDecl> iter=cd.localMethodsIterator();iter.hasNext();) {
				MethodDecl md = iter.next();
				if(!md.isStatic())
					mds.add(md);
			}
		}
	}

	public String getName() {
		return "ExtractInterface";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(cd == null)
			status.addFatalError("Can only extract from classes.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		Program root = cd.programRoot();
		try {
			pm.beginTask("Performing refactoring...", 1);
			
			RefactoringUtil.recompileSourceCompilationUnits(root, cd);
			
			Program.startRecordingASTChangesAndFlush();
		
			cd.doExtractInterface(pkgName, ifaceName, mds);

			changes = RefactoringUtil.createChanges("ExtractInterface", Program.cloneUndoStack());
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

	public void setIfaceName(String name) {
		ifaceName = name;
	}

	public void addMethod(MethodDecl md) {
		mds.add(md);
	}

	public void removeMethod(MethodDecl md) {
		mds.remove(md);
	}

	public Collection<MethodDecl> getMethods() {
		return mds;
	}

	public String getPackageName() {
		return pkgName;
	}

	public void setPackageName(String name) {
		pkgName = name;
	}

	public String getIfaceName() {
		return ifaceName;
	}
}
