package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.editor.debug.JastAddJBreakpointAdapter;

public class JastAddJEditor extends JastAddEditor {
	
	public static final String EDITOR_ID = "org.jastadd.plugin.jastaddj.editor.JastAddJEditor";
	
	private JastAddJBreakpointAdapter breakpointAdapter;
	
	public Object getAdapter(Class required) {
		if (IToggleBreakpointsTarget.class.equals(required)) {
			if (breakpointAdapter == null) {
				breakpointAdapter = new JastAddJBreakpointAdapter(this);
			}
			return breakpointAdapter;
		}
		return super.getAdapter(required);
	}
}
