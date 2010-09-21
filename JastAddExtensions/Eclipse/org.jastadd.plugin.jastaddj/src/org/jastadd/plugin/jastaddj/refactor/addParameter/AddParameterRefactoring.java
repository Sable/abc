package org.jastadd.plugin.jastaddj.refactor.addParameter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.refactor.RefactoringUtil;

import AST.ASTNode;
import AST.Literal;
import AST.MethodDecl;
import AST.NullLiteral;
import AST.ParameterDeclaration;
import AST.Program;
import AST.RefactoringException;
import AST.TypeDecl;
import beaver.Parser;
import beaver.Parser.Exception;

public class AddParameterRefactoring extends Refactoring {

	private IJastAddNode selectedNode;
	private String name;
	private String type;
	private String value;
	private RefactoringStatus status;
	private Change changes;

	public AddParameterRefactoring(IJastAddNode selectedNode) {
		super();
		if(selectedNode instanceof MethodDecl) {
			this.selectedNode = selectedNode;
		} else {
			this.selectedNode = null;
		}
	}

	public String getName() {
		return "AddParameterRefactoring";
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		if(!(selectedNode instanceof MethodDecl))
			status.addFatalError("Please select a method.");
		return status;
	}

	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		status = new RefactoringStatus();

		MethodDecl md = (MethodDecl)selectedNode;
		Program root = md.programRoot();
		
		TypeDecl td = root.findType(type);
		if(td == null) {
			status.addFatalError("Couldn't find type " + type);
			return status;
		}
		
		if(!ASTNode.isValidName(name)) {
			status.addFatalError("Invalid name.");
			return status;
		}

		ByteArrayInputStream is = new ByteArrayInputStream(value.getBytes());
		scanner.JavaScanner scanner = new scanner.JavaScanner(new scanner.Unicode(is));
		Literal defaultValue = null;
		try {
			Object expr = new parser.JavaParser().parse(scanner, parser.JavaParser.AltGoals.expression);
			if(!(expr instanceof Literal)) {
				status.addFatalError("Default value should be a literal.");
				return status;
			}
			defaultValue = (Literal)expr;
		} catch(Parser.Exception exc) {
			status.addFatalError("Cannot parse default value.");
			return status;
		} catch(IOException exc) {
			status.addFatalError("Cannot parse default value due to IOException.");
			return status;
		}
	
		try {
			pm.beginTask("Performing refactoring...", 1);

			RefactoringUtil.recompileSourceCompilationUnits(root, selectedNode);
			Program.startRecordingASTChangesAndFlush();

			md.doAddParameter(new ParameterDeclaration(td.createLockedAccess(), name), md.getNumParameter(), defaultValue, false);	

			changes = RefactoringUtil.createChanges("AddParameter", Program.cloneUndoStack());
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

	public void setType(String type) {
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDefaultValue(String value) {
		this.value = value;
	}
}
