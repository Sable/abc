package org.jastadd.plugin.search;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

public class JastAddSearchQuery implements ISearchQuery {

	private JastAddSearchResult result;
	
	@SuppressWarnings("unchecked")
	public JastAddSearchQuery(Collection coll, String label) {
		result = new JastAddSearchResult(this, coll, label);
	}
	
	public boolean canRerun() {
		return false;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public String getLabel() {
		return "JastAdd Search Query";
	}

	public ISearchResult getSearchResult() {
		return result;
	}

	public IStatus run(IProgressMonitor monitor)
			throws OperationCanceledException {
		return new JastAddSearchStatus();
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
