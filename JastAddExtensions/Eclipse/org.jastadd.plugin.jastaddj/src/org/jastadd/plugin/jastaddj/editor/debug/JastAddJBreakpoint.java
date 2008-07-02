package org.jastadd.plugin.jastaddj.editor.debug;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.jastadd.plugin.jastaddj.JastAddJActivator;
import org.jastadd.plugin.model.JastAddModel.FileInfo;

public class JastAddJBreakpoint extends LineBreakpoint {

	public static final String MARKER_ID = "org.jastadd.plugin.jastaddj.BreakpointMarker";
	//public static final String MARKER_ID = "org.eclipse.jdt.debug.javaLineBreakpointMarker";
	protected FileInfo fileInfo;
	protected String typeName;
	protected int lineNumber;
	
	public JastAddJBreakpoint(final IResource resource, String typeName, FileInfo fileInfo, 
			final int lineNumber, final Map<String,Object> attributes) throws DebugException {
		this.fileInfo = fileInfo;
		this.lineNumber = lineNumber;
		this.typeName = typeName;
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(MARKER_ID));
				// add attributes
				attributes.put(IBreakpoint.ID, getModelIdentifier());
				attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(true));
				attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
				attributes.put(IMarker.CHAR_START, new Integer(-1));
				attributes.put(IMarker.CHAR_END, new Integer(-1));
				// set attributes
				ensureMarker().setAttributes(attributes);
				// add to breakpoint manager if requested
				register(true);
			}
		};
		run(getMarkerRule(resource), wr);
	}
	
	protected void register(boolean register) throws CoreException {
        DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null && register) {
            plugin.getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}	
	
	@Override
	public String getModelIdentifier() {
		return JastAddJActivator.JASTADDJ_PLUGIN_ID;
	}

	public boolean sameAs(String name, FileInfo info, int line) {
		return typeName.equals(name) &&
			fileInfo.second.equals(info.second) && 
			lineNumber == line;
	}
}
