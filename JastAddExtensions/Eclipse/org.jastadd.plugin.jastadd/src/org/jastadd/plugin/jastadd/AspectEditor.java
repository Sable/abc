package org.jastadd.plugin.jastadd;

import org.eclipse.jface.action.IMenuManager;
import org.jastadd.plugin.editor.JastAddEditor;
import org.jastadd.plugin.jastaddj.editor.JastAddJEditor;

public class AspectEditor extends JastAddJEditor {
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.AspectEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.AspectEditorContext";
	

	@Override
	protected void populateFindContextMenuItems(IMenuManager findMenu,
			JastAddEditor editor) {
		super.populateFindContextMenuItems(findMenu, editor);
		
		addContextMenuItem(findMenu, "Find &Equations",
				"org.jastadd.plugin.jastadd.find.FindEquations",
				new FindEquationsHandler());
	}

}
