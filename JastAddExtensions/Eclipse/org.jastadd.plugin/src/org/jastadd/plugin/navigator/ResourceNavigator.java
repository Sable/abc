package org.jastadd.plugin.navigator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;

public class ResourceNavigator extends org.eclipse.ui.views.navigator.ResourceNavigator {
    protected void initFilters(TreeViewer viewer) {
    	super.initFilters(viewer);
    	ResourcePatternFilter filter = new ResourcePatternFilter();
    	filter.setPatterns(new String[] { ".project", "*.java.dummy", "*.class" });
        viewer.addFilter(filter);
    }

}
