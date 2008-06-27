package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastaddj.JastAddJActivator;

/**
 * Represents an attribute with its value, if computed.
 * @author luke
 *
 */
public class AttributeEvaluationNode {

	private String attribute;
	private IJavaVariable parent;
	private boolean calculated = false;
	private IJavaVariable result;
	
	public AttributeEvaluationNode(IJavaVariable parentVariable, String attribute) {
		this.attribute = attribute;
		this.parent = parentVariable;
		// Check, from parent, whether we already have a value for this attribute
		try {
			for (IVariable child : parent.getValue().getVariables()) {
				if (child.getName().startsWith(attribute + "$computed")) {
					calculated = child.getValue().getValueString().equals("true");
				}
				if (child.getName().startsWith(attribute + "$value")) {
					result = (IJavaVariable) child;
				}
			}
		} catch (DebugException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}
	
	public boolean hasBeenCalculated() {
		return calculated;
	}
	
	/**
	 * 
	 * @return variable, iff hasBeenCalculated = true
	 */
	public IJavaVariable getValue() {
		// If it's already been computed, we want to return the computed value
		if (hasBeenCalculated()) {
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * Evaluates the attribute.
	 * 
	 * If it has already been evaluated, it is evaluated again.
	 */
	public void eval() {
		// TODO evaluation code
	}

	public IJavaVariable getParentVariable() {
		return parent;
	}

	public String getAttributeName() {
		return attribute;
	}
	
	public static class AttributeEvaluationNested extends AttributeEvaluationNode {
		private AttributeEvaluationNode parent;
		
		public AttributeEvaluationNested(AttributeEvaluationNode parent, String attribute) {
			super(parent.getValue(), attribute);
			this.parent = parent;
		}

		public AttributeEvaluationNode getParent() {
			return parent;
		}
	}
}
