/**
 * 
 */
package org.jastadd.plugin.providers.model;

import java.util.Collection;

public class JastAddOnDemandTreeItem<T> {
	public T value;
	public T parent;
	public Collection<JastAddOnDemandTreeItem<T>> children;

	public JastAddOnDemandTreeItem(T value, JastAddOnDemandTreeItem<T> parent) {
		this.value = value;
	}
}