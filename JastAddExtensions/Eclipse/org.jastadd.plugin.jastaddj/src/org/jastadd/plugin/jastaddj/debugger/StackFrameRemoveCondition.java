package org.jastadd.plugin.jastaddj.debugger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * A condition for stack frames, that allows us to define valid/invalid frames.
 * @author luke
 *
 */
public interface StackFrameRemoveCondition {
	public boolean isValid(IStackFrame stackFrame) throws DebugException;
}
