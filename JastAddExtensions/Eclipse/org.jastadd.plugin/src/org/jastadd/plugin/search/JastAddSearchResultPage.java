package org.jastadd.plugin.search;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.jastadd.plugin.providers.JastAddContentProvider;

public class JastAddSearchResultPage extends AbstractTextSearchViewPage implements ISearchResultPage {

	public static final String SEARCH_ID = "org.jastadd.plugin.search.JastAddSearchResultPage";
	
	private JastAddContentProvider fContentProvider;
	
	@Override
	protected void clear() {
		/*
		if (fContentProvider != null)
			fContentProvider.clear();
		*/
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		/*
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
		*/
	}

}
