package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Shell;
import org.jastadd.plugin.jastadd.generated.AST.AttributeDecl;

/**
 * Represents an attribute with its value, if computed.
 * 
 * The state represents the state of the variable.
 * @author luke
 *
 */
public class AttributeEvaluationNode extends AttributeNode {

	private AttributeEvaluation eval;
	
	private AttributeNode parent;

	public AttributeEvaluationNode(AttributeNode parent, AttributeDecl attribute, IJavaThread thread, Shell shell) {
		eval = new AttributeEvaluation(parent.getCurrent(), attribute, thread, shell);
		this.parent = parent;
	}

	/**
	 * Returns the result of the evaluation.
	 * @return variable, iff getState().equals(AttributeState.CALCULATED) || getState().equals(AttributeState.PRE_CALCULATED)
	 */
	public IJavaValue getCurrent() {
		return eval.getCurrent();
	}

	/**
	 * Evaluates the attribute.
	 * 
	 * If it has already been evaluated, it is evaluated again.
	 * 
	 * Will not be evaluated if we're already executing this method
	 */
	public void eval() {
		eval.eval();	
	}

	@Override
	public AttributeNode getParent() {
		return parent;
	}

	@Override
	public String getValueString() {
		switch (eval.getState()) {
		case CALCULATED:
			return super.getValueString();
		case PRE_CALCULATED:
			return super.getValueString();
		case EMPTY:
			return "Unevaluated, double click to calculate";
		case BEING_CALCULATED:
			return "Currently executing";
		case NOT_CALCULABLE:
			return "Cannot calculate";
		}
		return "Invalid state";
	}
	
	@Override
	public String getNameString() {
		return eval.getNameString();
	}
}
