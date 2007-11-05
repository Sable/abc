/**
 * 
 */
package org.jastadd.plugin.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.providers.model.JastAddOnDemandTreeItem;

public abstract class JastAddBaseOnDemandTreeLabelProvider<T> extends LabelProvider {
	public Image getImage(Object element) {
		JastAddOnDemandTreeItem<T> item = (JastAddOnDemandTreeItem<T>) element;
		return computeImage(item);		
	}

	public String getText(Object element) {
		JastAddOnDemandTreeItem<T> item = (JastAddOnDemandTreeItem<T>) element;
		T value = item.value;
		return computeText(item);
	}
	
	protected abstract Image computeImage(JastAddOnDemandTreeItem<T> item);
	
	protected abstract String computeText(JastAddOnDemandTreeItem<T> item);
}