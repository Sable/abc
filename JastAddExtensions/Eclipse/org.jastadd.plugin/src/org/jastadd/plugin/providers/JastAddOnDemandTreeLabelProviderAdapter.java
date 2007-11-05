/**
 * 
 */
package org.jastadd.plugin.providers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.providers.model.JastAddOnDemandTreeItem;

public class JastAddOnDemandTreeLabelProviderAdapter extends JastAddBaseOnDemandTreeLabelProvider {
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