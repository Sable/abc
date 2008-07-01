package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastaddj.JastAddJActivator;

/**
 * Represents an attribute with its value, if computed.
 * 
 * If the value is computed, hasBeenCalculated will return true.
 * @author luke
 *
 */
public class AttributeEvaluationNode {

	public enum AttributeState {
		CALCULATED,
		BEING_CALCULATED,
		EMPTY
	}
	
	private AttributeDecl attribute;
	private IJavaValue parent;
	private AttributeState calculated = AttributeState.EMPTY;
	private IJavaValue result;
	private IJavaThread thread;

	public AttributeEvaluationNode(IJavaValue parentValue, AttributeDecl attribute, IJavaThread thread) {
		this.attribute = attribute;
		this.parent = parentValue;
		this.thread = thread;

		// We want to discover the initial state of the attribute
		try {
			if (alreadyRunning(parent)) {
				calculated = AttributeState.BEING_CALCULATED;
			} else {
				// Scan the local variables of the parent, to see if this attribute has been pre-calculated
				for (IVariable child : parent.getVariables()) {
					if (child.getName().startsWith(attribute.name() + "$computed")) {
						calculated = child.getValue().getValueString().equals("true") ? AttributeState.CALCULATED : AttributeState.EMPTY;
					}
					if (child.getName().startsWith(attribute.name() + "$value")) {
						result = ((IJavaValue) ((IJavaVariable) child).getValue());
					}
				}
			}
		} catch (DebugException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}

	private boolean alreadyRunning(IJavaValue object) throws DebugException {
		// Check to see whether this method is currently executing on the stack
		for (IThread newThread : thread.getDebugTarget().getThreads()) {
			for (IStackFrame stackFrame : newThread.getStackFrames()) {
				IJavaStackFrame javaStackFrame = (IJavaStackFrame) stackFrame;
				if 		(javaStackFrame.getMethodName().equals(attribute.name()) &&
						javaStackFrame.getArgumentTypeNames().isEmpty() &&
						javaStackFrame.getReferenceType().equals(object.getJavaType())) {
					// We're executing this method, so set calculating switch
					return true;
				}
			}
		}
		return false;
	}
	
	public AttributeState getState() {
		return calculated;
	}

	/**
	 * 
	 * @return variable, iff getState().equals(AttributeState.CALCULATED)
	 */
	public IJavaValue getResult() {
		// If it's already been computed, we want to return the computed value
		if (getState().equals(AttributeState.CALCULATED)) {
			return result;
		// TODO return something else if we are currently in the process of calculating it
		} else {
			return null;
		}
	}

	/**
	 * Evaluates the attribute.
	 * 
	 * If it has already been evaluated, it is evaluated again.
	 * 
	 * Will not be evaluated if we're already executing this method
	 */
	public void eval() {
		try {
			// Ensure we're not trying to invoke an already running method
			if (calculated.equals(AttributeState.EMPTY)) {
				if (parent instanceof IJavaObject) {
					IJavaObject object = (IJavaObject) parent;
					
					result = object.sendMessage(attribute.name(), attribute.descName(), new IJavaValue[0], thread, null);
					calculated = AttributeState.CALCULATED;
				} else {
					// TODO error, we've tried to do this on a primitive? presumably they can't have attributes
				}
			} else {
				// We're not in a state to calculate the value
			}
		} catch (CoreException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
		}		
	}

	public IJavaValue getParentValue() {
		return parent;
	}

	public String getAttributeName() {
		return attribute.name();
	}

	public static class AttributeEvaluationNested extends AttributeEvaluationNode {
		private AttributeEvaluationNode parent;

		/**
		 * 
		 * @param parent - must have been calculated
		 * @param attribute
		 * @param viewer
		 */
		public AttributeEvaluationNested(AttributeEvaluationNode parent, AttributeDecl attribute, IJavaThread thread) {
			super(parent.getResult(), attribute, thread);
			this.parent = parent;
		}

		public AttributeEvaluationNode getParent() {
			return parent;
		}
	}
}
