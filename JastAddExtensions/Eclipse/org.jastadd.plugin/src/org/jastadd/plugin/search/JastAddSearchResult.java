package org.jastadd.plugin.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

public class JastAddSearchResult extends AbstractTextSearchResult implements ISearchResult {

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public ISearchQuery getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTooltip() {
		// TODO Auto-generated method stub
		return null;
	}
}
