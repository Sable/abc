package org.jastadd.plugin.jastaddj.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

public class RenameDelegateMethodStackFrame extends FilteredJavaStackFrame {

	JDIModelPresentation presentation = new JDIModelPresentation();
	
	public RenameDelegateMethodStackFrame(IJavaStackFrame stackFrame) {
		super(stackFrame);
	}
	
	public String getMethodName() throws DebugException {
		String methodName = stackFrame.getMethodName();
		return methodName.substring(methodName.lastIndexOf("$") + 1);
	}
	
	public List getArgumentTypeNames() throws DebugException {
		List args = new ArrayList<String>(stackFrame.getArgumentTypeNames());
		args.remove(0);
		return args;
	}
	
	public String getDeclaringTypeName() throws DebugException {
		return stackFrame.getDeclaringTypeName() + "$" + presentation.removeQualifierFromGenericName(stackFrame.getArgumentTypeNames().get(0).toString());
	}

	public String getReceivingTypeName() throws DebugException {
		return stackFrame.getReceivingTypeName() + "$" + presentation.removeQualifierFromGenericName(stackFrame.getArgumentTypeNames().get(0).toString());
	}
}
