package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.jdt.debug.core.IJavaValue;
import org.jastadd.plugin.jastadd.debugger.attributes.AttributeEvaluationNode.AttributeState;

public interface AttributeNode {

	public abstract IJavaValue getResult();

	public abstract IJavaValue getParentValue();

	public abstract String getAttributeName();
	
	public abstract AttributeState getState();

}