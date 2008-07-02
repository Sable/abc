package org.jastadd.plugin.jastadd.debugger.attributes;

import org.eclipse.jdt.debug.core.IJavaValue;

public interface AttributeNode {

	public abstract IJavaValue getResult();

	public abstract IJavaValue getParentValue();

	public abstract String getAttributeName();

}