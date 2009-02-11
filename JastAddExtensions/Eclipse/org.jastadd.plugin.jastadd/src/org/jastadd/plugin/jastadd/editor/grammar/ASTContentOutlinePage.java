package org.jastadd.plugin.jastadd.editor.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jastadd.plugin.compiler.ast.IASTNode;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.compiler.ast.IOutlineNode;
import org.jastadd.plugin.jastaddj.AST.ICompilationUnit;
import org.jastadd.plugin.ui.view.JastAddContentProvider;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;

public class ASTContentOutlinePage extends ContentOutlinePage {

	protected AbstractTextEditor fTextEditor;
	protected ASTDeclContainer fContainer;
	protected OutlineNodeComparator fComparator;
	private ITreeContentProvider fContentProvider;
	private IBaseLabelProvider fLabelProvider;
	
	public ASTContentOutlinePage(AbstractTextEditor editor) {
		fContainer = new ASTDeclContainer();
		fComparator = new OutlineNodeComparator();
	    fTextEditor = editor;
		fContentProvider = new ASTContentProvider();
		fLabelProvider = new JastAddLabelProvider();
	}

	/**
	 * Updates the AST list
	 * @param rootList The new AST list
	 */
	public void updateASTList(ArrayList<IASTNode> rootList) {
		fContainer.nodeList.clear();
		for (IASTNode ast : rootList) {
			if (ast instanceof IOutlineNode) {
				IOutlineNode node = (IOutlineNode)ast;
				//fContainer.nodeList.addAll(node.outlineChildren());
				fContainer.nodeList.add(node);
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
				//viewer.expandToLevel(3);
				control.setRedraw(true);
			}
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			IStructuredSelection structSelect = (IStructuredSelection)selection; 
			Object obj = structSelect.getFirstElement();
			if (obj instanceof IJastAddNode) {
				IJastAddNode node = (IJastAddNode)obj;				
				highlightNodeInEditor(node);
			}
		}
	}
	
	protected void highlightNodeInEditor(IJastAddNode node) {
		if (fContainer.nodeList.size() > 0 && 
				!(fContainer.nodeList.get(0) instanceof ICompilationUnit))
			return;
		ICompilationUnit unit = (ICompilationUnit)fContainer.nodeList.get(0);
		int startOffset = unit.offset(node.getBeginLine(), node.getBeginColumn());
		int endOffset = unit.offset(node.getEndLine(), node.getEndColumn());
		int length = endOffset - startOffset;
		if (startOffset >= 0) {
			fTextEditor.setHighlightRange(startOffset, length > 0 ? length : 0, true);
		} else {
			fTextEditor.setHighlightRange(0,0,true);
		}
	}

	
	protected class OutlineNodeComparator implements Comparator<IOutlineNode> {
		@Override
		public int compare(IOutlineNode arg0, IOutlineNode arg1) {
			return arg0.contentOutlineLabel().compareTo(arg1.contentOutlineLabel());
		}
	}
	
	protected class ASTDeclContainer {
		public ArrayList<IOutlineNode> nodeList = new ArrayList<IOutlineNode>();
	}
	
	protected class ASTContentProvider extends JastAddContentProvider {

		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof ASTDeclContainer) {
				ArrayList<IOutlineNode> nodeList = new ArrayList<IOutlineNode>();
				for (IOutlineNode node  : ((ASTDeclContainer)element).nodeList) {
					nodeList.addAll(node.outlineChildren());
				}
				Collections.sort(nodeList, fComparator);
				return nodeList.toArray(new IOutlineNode[0]);
			}
			return super.getChildren(element);
		}

		@Override
		public Object[] getElements(Object element) {
			if (element instanceof ASTDeclContainer) {
				ArrayList<IOutlineNode> nodeList = new ArrayList<IOutlineNode>();
				for (IOutlineNode node  : ((ASTDeclContainer)element).nodeList) {
					nodeList.addAll(node.outlineChildren());
				}
				Collections.sort(nodeList, fComparator);
				return nodeList.toArray(new IOutlineNode[0]);
			}
			return super.getElements(element);
		}
	}
}
