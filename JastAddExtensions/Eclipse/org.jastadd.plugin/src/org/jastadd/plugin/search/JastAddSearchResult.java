package org.jastadd.plugin.search;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;


public class JastAddSearchResult extends AbstractTextSearchResult implements ISearchResult {

	private Collection fResults;

	public JastAddSearchResult(Collection results) {
		fResults = results;
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
		return "JastAdd Search Result";
	}

	public ISearchQuery getQuery() {
		return new JastAddQuery(this);
	}

	public String getTooltip() {
		return "JastAdd Search Result Tooltip";
	}

	public void setResult(Collection results) {
		fResults = results;
	}
	
	public Collection getResults() {
		return fResults == null ? new LinkedList() : fResults;
	}
	
	
	private class JastAddQuery implements ISearchQuery {

		private JastAddSearchResult result;
		
		public JastAddQuery(JastAddSearchResult result) {
			this.result = result;
		}
		
		public boolean canRerun() {
			return false;
		}

		public boolean canRunInBackground() {
			return false;
		}

		public String getLabel() {
			return "JastAdd Search Query";
		}

		public ISearchResult getSearchResult() {
			return result;
		}

		public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
			return new JastAddSearchStatus();
		}
		
	}
	
	private class JastAddSearchStatus implements IStatus {

		public IStatus[] getChildren() {
			return new IStatus[0];
		}

		public int getCode() {
			return 0;
		}

		public Throwable getException() {
			return null;
		}

		public String getMessage() {
			return "JastAdd Search Status";
		}

		public String getPlugin() {
			return org.jastadd.plugin.Activator.PLUGIN_ID;
		}

		public int getSeverity() {
			return 0;
		}

		public boolean isMultiStatus() {
			return false;
		}

		public boolean isOK() {
			return true;
		}

		public boolean matches(int severityMask) {
			return false;
		}
		
	}
}
