/**
 * 
 */
package org.jastadd.plugin.ui.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;

public abstract class BaseOnDemandTreeLabelProvider<T> extends LabelProvider {
	@SuppressWarnings("unchecked")
	public Image getImage(Object element) {
		JastAddOnDemandTreeItem<T> item = (JastAddOnDemandTreeItem<T>) element;
		return computeImage(item);		
	}

	@SuppressWarnings("unchecked")
	public String getText(Object element) {
		JastAddOnDemandTreeItem<T> item = (JastAddOnDemandTreeItem<T>) element;
		//T value = item.value;
		return computeText(item);
	}
	
	protected abstract Image computeImage(JastAddOnDemandTreeItem<T> item);
	
	protected abstract String computeText(JastAddOnDemandTreeItem<T> item);
}