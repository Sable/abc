package org.jastadd.plugin.search;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jastadd.plugin.EditorTools;
import org.jastadd.plugin.providers.JastAddLabelProvider;

import AST.ASTNode;

public class JastAddSearchResultPage extends AbstractTextSearchViewPage implements ISearchResultPage {

	public static final String SEARCH_ID = "org.jastadd.plugin.search.JastAddSearchResultPage";
	
	private JastAddSearchContentProvider fContentProvider;
	private JastAddSearchResult fInput;

	private TreeViewer fViewer;
	
	public JastAddSearchResultPage() {
		fContentProvider = new JastAddSearchContentProvider();
	}
	
	@Override
	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		// TODO Auto-generated method stub
		
	}
		
	private class JastAddSearchContentProvider implements ITreeContentProvider {

		private Object[] content = null;
		
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof ASTNode) {
				ASTNode node = (ASTNode)parentElement;
				return node.outlineChildren().toArray();
			} 
			return null;
		}

		public void clear() {
			content = null;
		}

		public Object getParent(Object element) {
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				ASTNode parent = node.getParent();
				if (parent != null && parent.showInContentOutline())
					return parent;
				else getParent(parent);
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				return !node.outlineChildren().isEmpty();
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return content;
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof JastAddSearchResult) {
				content = ((JastAddSearchResult)newInput).getElements();
			}			
		}

		public void elementsChanged(Object[] objects) {
			content = objects;
		}
	}
	
	/*
	private class JastAddSearchLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof String) {
				return (String)element;
			} 
			return "";
		}
	}
	*/
	
	private class JastAddDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object element = selection.getFirstElement();

			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				EditorTools.openFile(node);
			}
		}
	}
	
	@Override
	protected TreeViewer createTreeViewer(Composite parent) {
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		return fViewer;
	}
	
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setContentProvider(new JastAddSearchContentProvider());
		viewer.setLabelProvider(new JastAddLabelProvider());
		viewer.addDoubleClickListener(new JastAddDoubleClickListener());
		if (fInput != null)
			viewer.setInput(fInput);
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}
}
