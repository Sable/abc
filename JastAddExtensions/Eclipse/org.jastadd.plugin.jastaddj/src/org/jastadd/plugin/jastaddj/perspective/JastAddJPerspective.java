package org.jastadd.plugin.jastaddj.perspective;

import org.jastadd.plugin.jastaddj.navigator.JastAddJNavigator;
import org.jastadd.plugin.perspective.JastAddPerspectiveFactory;

public class JastAddJPerspective extends JastAddPerspectiveFactory {

	@Override
	protected String getNavigatorID() {
		return JastAddJNavigator.NAVIGATOR_ID;
	}

}
