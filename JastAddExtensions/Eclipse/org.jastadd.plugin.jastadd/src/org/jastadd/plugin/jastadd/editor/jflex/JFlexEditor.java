package org.jastadd.plugin.jastadd.editor.jflex;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class JFlexEditor extends AbstractDecoratedTextEditor {
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.JFlexEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.JFlexEditorContext";
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		setEditorContextMenuId(getEditorSite().getId());		
		 // Set the source viewer configuration before the call to createPartControl to set viewer configuration	
	    super.setSourceViewerConfiguration(new JFlexSourceViewerConfiguration());
	    super.createPartControl(parent);
	}
}
