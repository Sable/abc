/**
 * 
 */
package org.jastadd.plugin.ui.view;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.search.JastAddOnDemandTreeItem;

public class JastAddOnDemandTreeLabelProviderAdapter extends BaseOnDemandTreeLabelProvider {
	ILabelProvider provider;
	
	public JastAddOnDemandTreeLabelProviderAdapter(ILabelProvider provider) {
		this.provider = provider;
	}
	
	protected Image computeImage(JastAddOnDemandTreeItem item) {
		return provider.getImage(item.value);
	}
	
	protected String computeText(JastAddOnDemandTreeItem item) {
		return provider.getText(item.value);			
	}
}