package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.editor.JastAddEditorContributor;

public class ParserEditorContributor extends JastAddEditorContributor {

	@Override
	protected String getEditorContextID() {
		return ParserEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	protected String getEditorID() {
		return ParserEditor.EDITOR_ID;
	}

	@Override
	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
	}
}
