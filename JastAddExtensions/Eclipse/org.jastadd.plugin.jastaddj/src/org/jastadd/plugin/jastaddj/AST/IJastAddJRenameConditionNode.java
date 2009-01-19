package org.jastadd.plugin.jastaddj.AST;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public interface IJastAddJRenameConditionNode {
	Change checkRenameConditions(String name, RefactoringStatus status);
}
