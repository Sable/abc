package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.jastaddj.explorer.JastAddJExplorer;

public class Explorer extends JastAddJExplorer {
	
	public static final String VIEW_ID = "org.jastadd.plugin.jastadd.Explorer";
	
	protected String[] filterNames = {"flex.xml", "parser.xml"};
	
	protected boolean shouldBeFiltered(String resourceName) {	
		for (int i = 0; i < filterNames.length; i++) {
			if (resourceName.equals(filterNames[i])) {
				return true;
			}
		}
		return super.shouldBeFiltered(resourceName);
	}
}
