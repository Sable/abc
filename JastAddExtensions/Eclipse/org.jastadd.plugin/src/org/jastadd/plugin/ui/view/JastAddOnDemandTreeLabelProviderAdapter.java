/**
 * 
 */
package org.jastadd.plugin.ui.view;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;

public class JastAddOnDemandTreeLabelProviderAdapter<T> extends BaseOnDemandTreeLabelProvider<T> {
	ILabelProvider provider;
	
	public JastAddOnDemandTreeLabelProviderAdapter(ILabelProvider provider) {
		this.provider = provider;
	}
	
	protected Image computeImage(JastAddOnDemandTreeItem<T> item) {
		return provider.getImage(item.value);
	}
	
	protected String computeText(JastAddOnDemandTreeItem<T> item) {
		return provider.getText(item.value);			
	}
}