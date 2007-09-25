package org.jastadd.plugin.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.jastadd.plugin.AST.ASTNode;

public class JastAddLabelProvider extends LabelProvider {
	
	private LabelProvider parent;
	
	public JastAddLabelProvider(LabelProvider parent) {
		this.parent = parent;
	}
	
	public JastAddLabelProvider() {
		parent = new LabelProvider();
	}
	
	public String getText(Object element) {
		if (element instanceof ASTNode) {
			return ((ASTNode)element).contentOutlineLabel();
		}
		return parent.getText(element);
	}
	
	public Image getImage(Object element) {
		if(element instanceof ASTNode) {
			ASTNode node = (ASTNode)element;
			return node.contentOutlineImage();
		}
		return parent.getImage(element);
	}
}
