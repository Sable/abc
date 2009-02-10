package org.jastadd.plugin.jastadd.editor.grammar;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.editor.JastAddJContentOutlinePage;
import org.jastadd.plugin.ui.view.JastAddContentProvider;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;

public class ASTContentOutlinePage extends ContentOutlinePage {

	protected AbstractTextEditor fTextEditor;
	protected ASTDeclContainer fContainer;
	private ITreeContentProvider fContentProvider;
	private IBaseLabelProvider fLabelProvider;

	
	public ASTContentOutlinePage(AbstractTextEditor editor) {
		fContainer = new ASTDeclContainer();
	    fTextEditor = editor;
		fContentProvider = new ASTContentProvider();
		fLabelProvider = new JastAddLabelProvider();
	}

	public void updateASTList(ArrayList<IASTNode> rootList) {
		fContainer.nodeList.clear();
		for (IASTNode ast : rootList) {
			if (ast instanceof IOutlineNode) {
				IOutlineNode node = (IOutlineNode)ast;
				fContainer.nodeList.addAll(node.outlineChildren());
			}
		}
		update();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(fContentProvider);
		viewer.setLabelProvider(fLabelProvider);
		viewer.addSelectionChangedListener(this);
		update();
	}

	/**
	 * Redraws the tree view 
	 */
	public void update() {
		TreeViewer viewer = getTreeViewer();
		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fContainer); 
				viewer.expandToLevel(3);
				control.setRedraw(true);
			}
		}
	}
	
	protected class ASTDeclContainer {
		public ArrayList<IOutlineNode> nodeList = new ArrayList<IOutlineNode>();
	}
	
	protected class ASTContentProvider extends JastAddContentProvider {

		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof ASTDeclContainer) {
				return ((ASTDeclContainer)element).nodeList.toArray(new IOutlineNode[0]);
			}
			return super.getChildren(element);
		}

		@Override
		public Object[] getElements(Object element) {
			if (element instanceof ASTDeclContainer) {
				return ((ASTDeclContainer)element).nodeList.toArray(new IOutlineNode[0]);
			}
			return super.getElements(element);
		}
		
		
		
	}
}
