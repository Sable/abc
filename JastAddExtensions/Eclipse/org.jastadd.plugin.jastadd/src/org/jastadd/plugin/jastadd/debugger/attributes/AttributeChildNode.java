package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.jastadd.plugin.jastadd.Activator;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluationNode.AttributeState;
import org.jastadd.plugin.jastaddj.JastAddJActivator;

public class AttributeChildNode implements AttributeNode {

	private IJavaVariable variable;
	private IJavaValue parentValue;
	
	public AttributeChildNode(IJavaVariable variable, IJavaValue parentValue) {
		this.variable = variable;
		this.parentValue = parentValue;
	}
	
	
	@Override
	public String getAttributeName() {
		try {
			return variable.getName();
		} catch (DebugException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			return "";
		}
	}

	@Override
	public IJavaValue getParentValue() {
		return parentValue;
	}

	@Override
	public IJavaValue getResult() {
		try {
			return (IJavaValue) variable.getValue();
		} catch (DebugException e) {
			ILog log = Platform.getLog(JastAddJActivator.getInstance().getBundle());
			log.log(new Status(IStatus.ERROR, Activator.JASTADD_PLUGIN_ID, e.getLocalizedMessage(), e));
			return null;
		}
	}
	
	public AttributeState getState() {
		return AttributeState.PRE_CALCULATED;
	}

	
	public static class AttributeChildNested extends AttributeChildNode {
		private AttributeNode parent;
		
		public AttributeChildNested(IJavaVariable variable,	AttributeNode node) {
			super(variable, node.getResult());
			this.parent = node;
		}

		public AttributeNode getParent() {
			return parent;
		}
		
	}
}
