package org.jastadd.plugin.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.AST.OutlineNode;

public class JastAddLabelProvider extends LabelProvider {
	
	private LabelProvider parent;
	
	public JastAddLabelProvider(LabelProvider parent) {
		this.parent = parent;
	}
	
	public JastAddLabelProvider() {
		parent = new LabelProvider();
	}
	
	public String getText(Object element) {
		if (element instanceof OutlineNode) {
			return ((OutlineNode)element).contentOutlineLabel();
		}
		return parent.getText(element);
	}
	
	public Image getImage(Object element) {
		if(element instanceof OutlineNode) {
			return ((OutlineNode)element).contentOutlineImage();
		}
		return parent.getImage(element);
	}
}
