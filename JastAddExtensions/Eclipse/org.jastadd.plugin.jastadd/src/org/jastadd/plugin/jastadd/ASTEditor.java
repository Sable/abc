package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.editor.JastAddEditor;

public class ASTEditor extends JastAddEditor {
	public static final String EDITOR_ID = "org.jastadd.plugin.jastadd.ASTEditor";
	public static final String EDITOR_CONTEXT_ID = "org.jastadd.plugin.jastadd.ASTEditorContext";
	@Override
	public String getEditorContextID() {
		return EDITOR_ID;
	}
}
