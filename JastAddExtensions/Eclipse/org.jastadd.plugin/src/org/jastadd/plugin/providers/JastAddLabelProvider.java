package org.jastadd.plugin.providers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import AST.ASTNode;

public class JastAddLabelProvider extends LabelProvider {
	LabelProvider parent;
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
		/*
		String imageKey = null;
		if(element instanceof ASTDecl) {
			imageKey = ISharedImages.IMG_OBJ_FOLDER;
		}
		else if(element instanceof ListComponents) {
			imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT;
		}
		else if(element instanceof Components) {
			imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		}
		return imageKey == null ? null : PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		*/
		if(element instanceof ASTNode) {
			String imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OPEN_MARKER;
			return imageKey == null ? null : PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
		return parent.getImage(element);
	}
}
