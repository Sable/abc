package org.jastadd.plugin.jastadd.editor.parser;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class ParserEditor extends AbstractDecoratedTextEditor { //implements IASTRegistryListener {
	
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.ParserEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.ParserEditorContext";
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		setEditorContextMenuId(getEditorSite().getId());		
		 // Set the source viewer configuration before the call to createPartControl to set viewer configuration	
	    super.setSourceViewerConfiguration(new ParserSourceViewerConfiguration());
	    super.createPartControl(parent);
	}
}
