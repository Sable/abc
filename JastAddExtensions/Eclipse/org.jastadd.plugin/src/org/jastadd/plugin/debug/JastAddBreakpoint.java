package org.jastadd.plugin.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.LineBreakpoint;

public class JastAddBreakpoint extends LineBreakpoint {

	public JastAddBreakpoint(IResource resource, int i) {
		// TODO Auto-generated constructor stub
	}

	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return "org.jastadd.plugin";
	}
	
}
