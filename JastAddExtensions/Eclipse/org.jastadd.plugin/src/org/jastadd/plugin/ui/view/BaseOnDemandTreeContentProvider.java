/**
 * 
 */
package org.jastadd.plugin.ui.view;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;

public abstract class BaseOnDemandTreeContentProvider<T> implements ITreeContentProvider {
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		return new Object[] { new JastAddOnDemandTreeItem<T>((T)inputElement, null)};
	}

	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object parentElement) {
		JastAddOnDemandTreeItem<T> parentItem = (JastAddOnDemandTreeItem<T>) parentElement;

		if (parentItem.children == null)
			parentItem.children = computeChildren(parentItem);

		return parentItem.children.toArray();
	}

	@SuppressWarnings("unchecked")
	public Object getParent(Object element) {
		return ((JastAddOnDemandTreeItem<T>) element).parent;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public void dispose() {}
	
	protected abstract Collection<JastAddOnDemandTreeItem<T>> computeChildren(JastAddOnDemandTreeItem<T> item);		
}