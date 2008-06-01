package org.jastadd.plugin.jastaddj.AST;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.jastadd.plugin.model.JastAddModel;

public interface IJastAddJRenameConditionNode {
	Change checkRenameConditions(String name, RefactoringStatus status, JastAddModel model);
}
