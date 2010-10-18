package org.jastadd.plugin.jastaddj.refactor;

import java.util.Iterator;
import java.util.Stack;

import org.eclipse.ltk.core.refactoring.Change;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ICompiler;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.util.FileUtil;

import AST.ASTModification;
import AST.ASTNode;
import AST.ChangeAccumulator;
import AST.CompilationUnit;
import AST.Program;

public class RefactoringUtil {
	public static void recompileSourceCompilationUnits(Program root, IJastAddNode except) {
		java.util.LinkedList<CompilationUnit> exceptList = new java.util.LinkedList<CompilationUnit>();
		exceptList.add(((ASTNode) except).compilationUnit());
		recompileSourceCompilationUnits(root, exceptList);
	}

	public static void recompileSourceCompilationUnits(Program root, java.util.List<CompilationUnit> except) {
		Iterator cui = root.compilationUnitIterator();
		// assume the compilation unit of selected node doesn't need refreshing
		while (cui.hasNext()) {
			CompilationUnit cu = (CompilationUnit) cui.next();
			if (cu.fromSource() && !except.contains(cu)) {
				for (ICompiler compiler : Activator.getRegisteredCompilers()) {
					if (compiler.canCompile(FileUtil.getFile(cu.pathName()))) {
						compiler.compile(null, null, null, FileUtil.getFile(cu.pathName()));
					}
				}
			}
		}
		root.flushCaches();
	}
	
	public static Change createChanges(String name, Stack<ASTModification> undoStack) {
		ChangeAccumulator changeAccumulator = new ChangeAccumulator(name);
		changeAccumulator.addAllEdits(undoStack);
		return changeAccumulator.getChanges();
	}
}
