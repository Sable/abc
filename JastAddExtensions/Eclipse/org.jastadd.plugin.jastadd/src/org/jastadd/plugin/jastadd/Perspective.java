package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.jastaddj.perspective.JastAddJPerspective;

public class Perspective extends JastAddJPerspective {

	@Override
	protected String getNavigatorID() {
		return Explorer.VIEW_ID;
	}

}
