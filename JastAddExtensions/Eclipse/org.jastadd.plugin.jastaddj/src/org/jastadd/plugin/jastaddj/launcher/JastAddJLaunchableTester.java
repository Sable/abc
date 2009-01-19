package org.jastadd.plugin.jastaddj.launcher;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ICompiler;

public class JastAddJLaunchableTester extends PropertyTester {
	private static final String IS_JASTADDJ_PROJECT = "isJastAddJProject";

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (IS_JASTADDJ_PROJECT.equals(property)) {
			if (!(receiver instanceof IProject))
				return false;
			IProject project = (IProject)receiver;
			for (ICompiler compiler : Activator.getRegisteredCompilers()) {
				if (compiler.canCompile(project)) {
					return true;
				}
			}
		}

		return false;
	}

}
