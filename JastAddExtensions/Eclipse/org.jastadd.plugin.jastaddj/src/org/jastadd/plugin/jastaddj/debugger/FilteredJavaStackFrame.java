package org.jastadd.plugin.jastaddj.debugger;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaModifiers;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * A generated class (using Eclipse delegate generation) that wraps around a stack frame.
 * It allows the user to selectively re-implement some of the functionality, so as to
 * provide filtering or renaming.
 * @author luke
 *
 */
public class FilteredJavaStackFrame implements IJavaStackFrame {
	
	IJavaStackFrame stackFrame;
	
	public FilteredJavaStackFrame(IJavaStackFrame stackFrame) {
		this.stackFrame = stackFrame;
	}
	
	/**
	 * This is an adapted method - the delegate method returns the delegate, not this class.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaStackFrame.class || adapter == IJavaModifiers.class) {
			return this;
		}
		return stackFrame.getAdapter(adapter);
	}

	public boolean canDropToFrame() {
		return stackFrame.canDropToFrame();
	}

	public boolean canForceReturn() {
		return stackFrame.canForceReturn();
	}

	public boolean canResume() {
		return stackFrame.canResume();
	}

	public boolean canStepInto() {
		return stackFrame.canStepInto();
	}

	public boolean canStepOver() {
		return stackFrame.canStepOver();
	}

	public boolean canStepReturn() {
		return stackFrame.canStepReturn();
	}

	public boolean canStepWithFilters() {
		return stackFrame.canStepWithFilters();
	}

	public boolean canSuspend() {
		return stackFrame.canSuspend();
	}

	public boolean canTerminate() {
		return stackFrame.canTerminate();
	}

	public void dropToFrame() throws DebugException {
		stackFrame.dropToFrame();
	}

	public IJavaVariable findVariable(String variableName)
			throws DebugException {
		return stackFrame.findVariable(variableName);
	}

	public void forceReturn(IJavaValue value) throws DebugException {
		stackFrame.forceReturn(value);
	}

	public List getArgumentTypeNames() throws DebugException {
		return stackFrame.getArgumentTypeNames();
	}

	public int getCharEnd() throws DebugException {
		return stackFrame.getCharEnd();
	}

	public int getCharStart() throws DebugException {
		return stackFrame.getCharStart();
	}

	public IDebugTarget getDebugTarget() {
		return stackFrame.getDebugTarget();
	}

	public IJavaClassType getDeclaringType() throws DebugException {
		return stackFrame.getDeclaringType();
	}

	public String getDeclaringTypeName() throws DebugException {
		return stackFrame.getDeclaringTypeName();
	}

	public ILaunch getLaunch() {
		return stackFrame.getLaunch();
	}

	public int getLineNumber() throws DebugException {
		return stackFrame.getLineNumber();
	}

	public int getLineNumber(String stratum) throws DebugException {
		return stackFrame.getLineNumber(stratum);
	}

	public IJavaVariable[] getLocalVariables() throws DebugException {
		return stackFrame.getLocalVariables();
	}

	public String getMethodName() throws DebugException {
		return stackFrame.getMethodName();
	}

	public String getModelIdentifier() {
		return stackFrame.getModelIdentifier();
	}

	public String getName() throws DebugException {
		return stackFrame.getName();
	}

	public String getReceivingTypeName() throws DebugException {
		return stackFrame.getReceivingTypeName();
	}

	public IJavaReferenceType getReferenceType() throws DebugException {
		return stackFrame.getReferenceType();
	}

	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return stackFrame.getRegisterGroups();
	}

	public String getSignature() throws DebugException {
		return stackFrame.getSignature();
	}

	public String getSourceName() throws DebugException {
		return stackFrame.getSourceName();
	}

	public String getSourceName(String stratum) throws DebugException {
		return stackFrame.getSourceName(stratum);
	}

	public String getSourcePath() throws DebugException {
		return stackFrame.getSourcePath();
	}

	public String getSourcePath(String stratum) throws DebugException {
		return stackFrame.getSourcePath(stratum);
	}

	public IJavaObject getThis() throws DebugException {
		return stackFrame.getThis();
	}

	public IThread getThread() {
		return stackFrame.getThread();
	}

	public IVariable[] getVariables() throws DebugException {
		return stackFrame.getVariables();
	}

	public boolean hasRegisterGroups() throws DebugException {
		return stackFrame.hasRegisterGroups();
	}

	public boolean hasVariables() throws DebugException {
		return stackFrame.hasVariables();
	}

	public boolean isConstructor() throws DebugException {
		return stackFrame.isConstructor();
	}

	public boolean isFinal() throws DebugException {
		return stackFrame.isFinal();
	}

	public boolean isNative() throws DebugException {
		return stackFrame.isNative();
	}

	public boolean isObsolete() throws DebugException {
		return stackFrame.isObsolete();
	}

	public boolean isOutOfSynch() throws DebugException {
		return stackFrame.isOutOfSynch();
	}

	public boolean isPackagePrivate() throws DebugException {
		return stackFrame.isPackagePrivate();
	}

	public boolean isPrivate() throws DebugException {
		return stackFrame.isPrivate();
	}

	public boolean isProtected() throws DebugException {
		return stackFrame.isProtected();
	}

	public boolean isPublic() throws DebugException {
		return stackFrame.isPublic();
	}

	public boolean isStatic() throws DebugException {
		return stackFrame.isStatic();
	}

	public boolean isStaticInitializer() throws DebugException {
		return stackFrame.isStaticInitializer();
	}

	public boolean isStepping() {
		return stackFrame.isStepping();
	}

	public boolean isSuspended() {
		return stackFrame.isSuspended();
	}

	public boolean isSynchronized() throws DebugException {
		return stackFrame.isSynchronized();
	}

	public boolean isSynthetic() throws DebugException {
		return stackFrame.isSynthetic();
	}

	public boolean isTerminated() {
		return stackFrame.isTerminated();
	}

	public boolean isVarArgs() throws DebugException {
		return stackFrame.isVarArgs();
	}

	public void resume() throws DebugException {
		stackFrame.resume();
	}

	public void stepInto() throws DebugException {
		stackFrame.stepInto();
	}

	public void stepOver() throws DebugException {
		stackFrame.stepOver();
	}

	public void stepReturn() throws DebugException {
		stackFrame.stepReturn();
	}

	public void stepWithFilters() throws DebugException {
		stackFrame.stepWithFilters();
	}

	public boolean supportsDropToFrame() {
		return stackFrame.supportsDropToFrame();
	}

	public void suspend() throws DebugException {
		stackFrame.suspend();
	}

	public void terminate() throws DebugException {
		stackFrame.terminate();
	}

	public boolean wereLocalsAvailable() {
		return stackFrame.wereLocalsAvailable();
	}
}
