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
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.jastadd.plugin.util.FileInfo;

public class JastAddJBreakpoint extends JavaLineBreakpoint {

	public static final String MARKER_ID = "org.jastadd.plugin.jastaddj.BreakpointMarker";
	public static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker";
	
	protected FileInfo fileInfo;
	protected String typeName;
	protected int lineNumber;
	
	public JastAddJBreakpoint(final IResource resource, final String typeName, FileInfo fileInfo, 
			final int lineNumber, final Map<String,Object> attributes) throws DebugException {
		this.fileInfo = fileInfo;
		this.lineNumber = lineNumber;
		this.typeName = typeName;
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// Java stuff taken from JavaLineBreakpoint
				attributes.put(TYPE_NAME, typeName);
				attributes.put(SUSPEND_POLICY, new Integer(getDefaultSuspendPolicy()));
				
				// create the marker
				setMarker(resource.createMarker(MARKER_ID));
				//setMarker(resource.createMarker(JAVA_LINE_BREAKPOINT));
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
	
	/*
	@Override
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
		//return JastAddJActivator.JASTADDJ_PLUGIN_ID;
	}
	*/

	public boolean sameAs(String name, FileInfo info, int line) {
		return typeName.equals(name) &&
			fileInfo.second.equals(info.second) && 
			lineNumber == line;
	}
}
