package org.jastadd.plugin.jastaddj.debugger;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

public class StackFrameRemoveCombine implements StackFrameRemoveCondition {

	List<StackFrameRemoveCondition> conds = new LinkedList<StackFrameRemoveCondition>();
	
	public boolean isValid(IStackFrame stackFrame)  throws DebugException {
		for (StackFrameRemoveCondition cond : conds) {
			if (!cond.isValid(stackFrame)) {
				return false;
			}
		}
		return true;
	}

	public void addStackFrameRemoveCondition(StackFrameRemoveCondition cond) {
		conds.add(cond);
	}
	
}
