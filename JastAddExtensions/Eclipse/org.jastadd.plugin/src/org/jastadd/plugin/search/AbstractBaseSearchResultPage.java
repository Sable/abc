package org.jastadd.plugin.search;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.jastadd.plugin.compiler.ast.IJastAddNode;

public abstract class AbstractBaseSearchResultPage extends AbstractTextSearchViewPage implements ISearchResultPage {

	
	
	private JastAddSearchContentProvider fContentProvider;
	//private JastAddSearchResult fInput;

	private TreeViewer fViewer;
	
	public AbstractBaseSearchResultPage() {
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
			if(parentElement instanceof JastAddNode) { 
				JastAddNode node = (JastAddNode)parentElement;
				return node.getChildren().toArray();
			} 
			return null;
		}

		public void clear() {
			content = null;
		}

		public Object getParent(Object element) {
			if(element instanceof JastAddNode) {
				JastAddNode node = (JastAddNode)element;
				JastAddNode parent = node.getParent();
				return parent;
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if(element instanceof JastAddNode) {
				JastAddNode node = (JastAddNode)element;
				return !node.getChildren().isEmpty();
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
	
	
	protected class JastAddSearchLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof JastAddNode) {
				return ((JastAddNode)element).getLabel();
			} 
			return "";
		}
		public Image getImage(Object element) {
			if(element instanceof JastAddNode) {
				return ((JastAddNode)element).getImage();
			}
			return null;
		}
	}
	
	protected class JastAddDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object element = selection.getFirstElement();

			if(element instanceof JastAddNode) {
				JastAddNode node = (JastAddNode)element;
				
				/*
				JastAddModel model = JastAddModelProvider.getModel(node.getJastAddNode());
				if (model != null)
					model.openFile(node.getJastAddNode());
				*/
				openEditorForNode(node.getJastAddNode());
			}
		}
	}
	
	protected abstract void openEditorForNode(IJastAddNode node);
	
	@Override
	protected TreeViewer createTreeViewer(Composite parent) {
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		return fViewer;
	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setContentProvider(new JastAddSearchContentProvider());
		viewer.setLabelProvider(new JastAddSearchLabelProvider());
		viewer.addDoubleClickListener(new JastAddDoubleClickListener());
//		if (fInput != null)
//			viewer.setInput(fInput);
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}
}
