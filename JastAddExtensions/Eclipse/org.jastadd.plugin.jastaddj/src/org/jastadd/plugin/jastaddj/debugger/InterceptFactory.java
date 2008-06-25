package org.jastadd.plugin.jastaddj.debugger;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class InterceptFactory implements IAdapterFactory {
	public static StackFrameRemoveCombine conds = new StackFrameRemoveCombine();
	
	// Define the invalid stack frames here
	static {
		// Condition to remove all those stack frames not intended for user
		// consumption
		conds.addStackFrameRemoveCondition(new StackFrameRemoveCondition() {
		
			public boolean isValid(IStackFrame stackFrame) throws DebugException {
				if (stackFrame instanceof IJavaStackFrame) {
					IJavaStackFrame javaStackFrame = (IJavaStackFrame) stackFrame;
					return !javaStackFrame.isSynthetic();
				}
				return stackFrame.getLineNumber() >= 0;
			}
			
		});
	}
	
	public Object getAdapter(Object adaptableObject, Class adapterType) {

		return new FilteredJavaThreadContentProvider(conds);
	}

	public Class[] getAdapterList() {
		return new Class[]{IElementContentProvider.class};
	}

}
