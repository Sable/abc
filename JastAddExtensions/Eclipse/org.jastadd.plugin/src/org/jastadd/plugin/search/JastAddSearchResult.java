package org.jastadd.plugin.search;

import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

public class JastAddSearchResult extends AbstractTextSearchResult implements ISearchResult {

	private JastAddSearchQuery fQuery;
	private Collection<JastAddNode> fResults;
	private String label;

	@SuppressWarnings("unchecked")
	public JastAddSearchResult(JastAddSearchQuery query, Collection results, String label) {
		fQuery = query;
		this.label = label;
		JastAddTreeFactory factory  = new JastAddTreeFactory();
		fResults = factory.buildTree(results);
	}
	
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getLabel() {
		return label;
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}

	public String getTooltip() {
		return "JastAdd Search Result Tooltip";
	}
	
	public Object[] getElements() {
		return fResults.toArray();
	}
}
