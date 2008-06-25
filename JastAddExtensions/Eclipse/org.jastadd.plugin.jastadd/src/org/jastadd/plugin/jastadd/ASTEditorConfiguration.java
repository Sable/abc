package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.model.JastAddEditorConfiguration;

public class ASTEditorConfiguration extends JastAddEditorConfiguration {
	public ASTEditorConfiguration(Model model) {
		this.model = model;
	}
	@Override
	public String getEditorContextID() {
		return ASTEditor.EDITOR_CONTEXT_ID;
	}
}
