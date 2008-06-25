package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.model.JastAddEditorConfiguration;

public class JFlexEditorConfiguration extends JastAddEditorConfiguration {
	public JFlexEditorConfiguration(Model model) {
		this.model = model;
	}
	@Override
	public String getEditorContextID() {
		return JFlexEditor.EDITOR_CONTEXT_ID;
	}
}
