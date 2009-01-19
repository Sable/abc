package org.jastadd.plugin.ui.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jastadd.plugin.compiler.ast.IOutlineNode;

public class JastAddLabelProvider extends LabelProvider {
	
	private LabelProvider parent;
	
	public JastAddLabelProvider(LabelProvider parent) {
		this.parent = parent;
	}
	
	public JastAddLabelProvider() {
		parent = new LabelProvider();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IOutlineNode) {
			//synchronized (((IJastAddNode)element).treeLockObject()) {
			return ((IOutlineNode)element).contentOutlineLabel();
			//}
		}
		return parent.getText(element);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if(element instanceof IOutlineNode) {
//			synchronized (((IJastAddNode)element).treeLockObject()) {
			return ((IOutlineNode)element).contentOutlineImage();
	//		}
		}
		return parent.getImage(element);
	}
}
