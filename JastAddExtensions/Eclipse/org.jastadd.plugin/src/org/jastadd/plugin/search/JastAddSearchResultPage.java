package org.jastadd.plugin.search;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
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

		private JastAddSearchResult result = null;
		
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof ASTNode) {
				ASTNode node = (ASTNode)parentElement;
				return node.outlineChildren().toArray();
			} 
			return null;
		}

		public void clear() {
			result = null;
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
			return new String[] {"Result1", "Result2"};
			//return fInput == null ? new Object[0] : result.getResults().toArray();
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof JastAddSearchResult) {
				result = (JastAddSearchResult)newInput;
			}			
		}

		public void elementsChanged(Object[] objects) {
			System.out.println("JastAddSearchContentProvider: elementsChanged");
		}
		
		public void newSearchResult(JastAddSearchResult result) {
			this.result = result;
		}
	}
	
	private class JastAddSearchLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof String) {
				return (String)element;
			} 
			return "";
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
		viewer.setLabelProvider(new JastAddSearchLabelProvider());
		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	public void setInput(ISearchResult newSearch, Object viewState) {
		/*
		if (newSearch != null && !(newSearch instanceof AbstractTextSearchResult))
			return; // ignore
		
		AbstractTextSearchResult oldSearch= fInput;
		if (oldSearch != null) {
			disconnectViewer();
			oldSearch.removeListener(fListener);
			AnnotationManagers.removeSearchResult(getSite().getWorkbenchWindow(), oldSearch);
		}
		fInput= (AbstractTextSearchResult) newSearch;
		
		if (fInput != null) {
			AnnotationManagers.addSearchResult(getSite().getWorkbenchWindow(), fInput);
			
			fInput.addListener(fListener);
			connectViewer(fInput);
			if (viewState instanceof ISelection)
				fViewer.setSelection((ISelection) viewState, true);
			else
				navigateNext(true);
			
			updateBusyLabel();
			turnOffDecoration();
			scheduleUIUpdate();
		}
		*/
		if (newSearch instanceof JastAddSearchResult) {
			fInput = (JastAddSearchResult)newSearch;
			fContentProvider.newSearchResult(fInput);
			fViewer.expandAll();
		} else fInput = null;
	}	

	@Override
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}
}
