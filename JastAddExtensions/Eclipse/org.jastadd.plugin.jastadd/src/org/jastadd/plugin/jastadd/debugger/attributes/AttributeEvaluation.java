package org.jastadd.plugin.jastadd.debugger.attributes;

import java.lang.Thread.State;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;
import org.jastadd.plugin.jastadd.generated.AST.ParameterDeclaration;

/**
 * Represents an attribute with its value, if computed.
 * 
 * The state represents the state of the variable.
 * @author luke
 *
 */
public class AttributeEvaluation {

	public enum AttributeState {
		/**
		 * A value is already held for this attribute, but it can be recalculated.
		 */
		CALCULATED,
		/**
		 * This attribute is currently being evaluating somewhere, and therefore cannot
		 * be displayed or calculated.
		 */
		BEING_CALCULATED,
		/**
		 * This attribute has no value, but can be evaluated to calculate one.
		 */
		EMPTY,
		/**
		 * This attribute has a pre-calculated value, which cannot be changed due to
		 * non-primitive arguments.
		 */
		PRE_CALCULATED,
		/**
		 * This attribute has no value, and cannot be evaluated to calculate one due to
		 * non-primitive arguments.
		 */
		NOT_CALCULABLE
	}

	private AttributeDecl attribute;
	private IJavaValue parent;
	private AttributeState state = AttributeState.EMPTY;
	private IJavaValue result;
	private IJavaThread thread;
	private Shell shell;

	public AttributeEvaluation(IJavaValue parent, AttributeDecl attribute, IJavaThread thread, Shell shell) {
		this.attribute = attribute;
		this.parent = parent;
		this.thread = thread;

		// We want to discover the initial state of the attribute
		try {
			if (alreadyRunning(parent)) {
				state = AttributeState.BEING_CALCULATED;
			} else {
				// Scan the local variables of the parent, to see if this attribute has been pre-calculated
				for (IVariable child : parent.getVariables()) {
					if (child.getName().startsWith(attribute.name() + "$computed")) {
						state = child.getValue().getValueString().equals("true") ? AttributeState.CALCULATED : AttributeState.EMPTY;
					}
					if (child.getName().startsWith(attribute.name() + "$value")) {
						result = ((IJavaValue) ((IJavaVariable) child).getValue());
					}
				}
			}

			// Check that each parameter is a primitive type, otherwise set the state to be uncomputable, for now.
			// (unless it has already been calculated)
			for (ParameterDeclaration param : attribute.getParameterList()){
				// We can only deal with primitive types or strings
				if (!param.type().isPrimitive() && !param.type().isString()) {
					state = (state == AttributeState.CALCULATED) ? AttributeState.PRE_CALCULATED : AttributeState.NOT_CALCULABLE;
				}
			}


		} catch (DebugException e) {
			AttributeUtils.recordError(e);
		}
	}

	private boolean alreadyRunning(IJavaValue object) throws DebugException {
		// Check to see whether this method is currently executing on the stack
		for (IThread newThread : thread.getDebugTarget().getThreads()) {
			for (IStackFrame stackFrame : newThread.getStackFrames()) {
				IJavaStackFrame javaStackFrame = (IJavaStackFrame) stackFrame;
				if 		(javaStackFrame.getMethodName().equals(attribute.name()) &&
						javaStackFrame.getReferenceType().equals(object.getJavaType()) &&
						javaStackFrame.getThis().equals(object)) {
					// We're executing this method, so set calculating switch
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the result of the evaluation.
	 * @return variable, iff getState().equals(AttributeState.CALCULATED) || getState().equals(AttributeState.PRE_CALCULATED)
	 */
	public IJavaValue getCurrent() {
		// If it's already been computed, we want to return the computed value
		if (state.equals(AttributeState.CALCULATED) || state.equals(AttributeState.PRE_CALCULATED)) {
			return result;
		} else {
			// Return nothing if we've not got a calculated result
			return null;
		}
	}

	public AttributeState getState() {
		return state;
	}

	private Exception contextSwitchException = null;
	
	/**
	 * Evaluates the attribute.
	 * 
	 * If it has already been evaluated, it is evaluated again.
	 * 
	 * Will not be evaluated if we're already executing this method
	 */
	public boolean eval() {
		try {
			// Ensure we're not trying to invoke an already running method
			if (state.equals(AttributeState.EMPTY) || state.equals(AttributeState.CALCULATED)) {
				IJavaValue current = parent;
				if (current != null && current instanceof IJavaObject) {
					final IJavaObject object = (IJavaObject) current;

					final ArrayList<IJavaValue> args = new ArrayList<IJavaValue>();

					// Deal with arguments
					if (attribute.getNumParameter() > 0) {

						IJavaDebugTarget javaDebugTarget = ((IJavaDebugTarget) object.getDebugTarget());

						ParameterDialog dialog = new ParameterDialog(shell, "Enter the parameters for evaluating this attribute", javaDebugTarget);
						for (ParameterDeclaration param : attribute.getParameterList()) {
							dialog.addField(param.name(), param.type().name(), "", false);
						}
						if (dialog.open() == IDialogConstants.CANCEL_ID) {
							// Cancel execution
							return false;
						}

						for (ParameterDeclaration param : attribute.getParameterList()) {
							String stringValue = dialog.getValue(param.name());

							IJavaValue arg = newPrimitiveValue(javaDebugTarget, stringValue, param.type().name());

							args.add(arg);
						}

					}


					try {

						class RunMethodEvaluation implements IRunnableWithProgress, IDebugContextListener {
							private IDebugContextService contextService;
							RunMethodEvaluation() {
								contextService = DebugUITools.getDebugContextManager().getContextService(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
								contextService.addDebugContextListener(this);
								
								shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							}

							private Boolean cancelDueToContextSwitch = false;

							@Override
							public void run(final IProgressMonitor monitor)	throws InvocationTargetException, InterruptedException {

								Thread childThread = new Thread("Run Debug Method") {
									@Override
									public void run() {
										try {
											IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();

											// store the breakpoint state
											Map<IBreakpoint, Boolean> stateRestore = new HashMap<IBreakpoint, Boolean>();
											for (IBreakpoint breakpoint : breakpoints) {
												stateRestore.put(breakpoint, breakpoint.isEnabled());
												breakpoint.setEnabled(false);
											}

											cancelDueToContextSwitch = false;
											result = object.sendMessage(attribute.name(), attribute.descName(), args.toArray(new IJavaValue[0]), thread, null);

											// restore the breakpoint state
											for (IBreakpoint breakpoint : breakpoints) {
												// if it's been manually re-enabled, since we disabled it, we respect that
												if (!breakpoint.isEnabled()) {
													breakpoint.setEnabled(stateRestore.get(breakpoint));	
												}
											}
										} catch (CoreException e) {
											AttributeUtils.recordError(e);
										}
									}
								};
								childThread.start();

								while (childThread.getState() != State.TERMINATED) {
									Thread.sleep(100);
									if (monitor.isCanceled() || cancelDueToContextSwitch) {
										if (cancelDueToContextSwitch) {
											contextSwitchException = new Exception("Evaluation of attribute failed due to change of debugging context");
										} else {
											childThread.stop();
										}
										throw new InterruptedException();
									}
								}
								contextService.removeDebugContextListener(this);
							}

							@Override
							public void debugContextChanged(DebugContextEvent event) {
								cancelDueToContextSwitch = true;
							}
						}

						IRunnableWithProgress op = new RunMethodEvaluation();

						new ProgressMonitorDialog(shell).run(true, true, op);
					} catch (InvocationTargetException e) {
						AttributeUtils.recordError(e, shell);
						return false;
					} catch (InterruptedException e) {
						if (contextSwitchException != null) {
							AttributeUtils.recordError(contextSwitchException, shell, "Evaluation could not complete");
							contextSwitchException = null;
						}
						// User cancelled the progress.
						return false;
					}



					state = AttributeState.CALCULATED;
					return true;
				} else {
					// error, we've tried to do this on a primitive
					Exception e = new Exception("Primitive variables can't execute attributes.");
					AttributeUtils.recordError(e);
				}
			} else {
				// We're not in a state to calculate the value
			}
		} catch (NonPrimitiveTypeException e) {
			AttributeUtils.recordError(e);
		}
		return false;
	}

	/**
	 * Converts a string representing the value to the type specified by the second string.
	 * @param target
	 * @param value
	 * @param type
	 * @return
	 * @throws NonPrimitiveTypeException
	 */
	protected static IJavaValue newPrimitiveValue(IJavaDebugTarget target, String value, String type) throws NonPrimitiveTypeException {

		if (type.equals("int")) {
			return target.newValue(Integer.valueOf(value));
		} else if (type.equals("double")) {
			return target.newValue(Double.valueOf(value));
		} else if (type.equals("float")) {
			return target.newValue(Float.valueOf(value));
		} else if (type.equals("long")) {
			return target.newValue(Long.valueOf(value));
		} else if (type.equals("short")) {
			return target.newValue(Short.valueOf(value));
		} else if (type.equals("boolean")) {
			return target.newValue(Boolean.valueOf(value));
		} else if (type.equals("byte")) {
			return target.newValue(Byte.valueOf(value));
		} else if (type.equals("String")) {
			return target.newValue(value);
		} else if (type.equals("char")) {
			return target.newValue(value.charAt(0));
		}

		throw new NonPrimitiveTypeException(type);
	}

	protected static class NonPrimitiveTypeException extends Exception {
		private static final long serialVersionUID = -8114268132764373368L;

		public NonPrimitiveTypeException(String type) {
			super(type + " is not a primitive or a string and therefore cannot be instantiated.");
		}

	}

	public String getValueString() {
		switch (state) {
		case CALCULATED:
			return result.toString();
		case PRE_CALCULATED:
			return result.toString();
		case EMPTY:
			return "Unevaluated, double click to calculate";
		case BEING_CALCULATED:
			return "Currently executing";
		case NOT_CALCULABLE:
			return "Cannot calculate";
		}
		return "Invalid state";
	}

	public String getNameString() {
		return attribute.name();
	}
}
