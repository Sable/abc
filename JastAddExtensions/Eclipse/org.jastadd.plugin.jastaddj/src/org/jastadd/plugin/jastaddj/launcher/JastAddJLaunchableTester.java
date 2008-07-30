package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddJLaunchableTester extends PropertyTester {
	private static final String IS_JASTADDJ_PROJECT = "isJastAddJProject";

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (IS_JASTADDJ_PROJECT.equals(property)) {
			if (!(receiver instanceof IProject))
				return false;
			IProject project = (IProject)receiver;
			return JastAddModelProvider.getModel(project, JastAddJModel.class) != null;
		}

		return false;
	}

}
