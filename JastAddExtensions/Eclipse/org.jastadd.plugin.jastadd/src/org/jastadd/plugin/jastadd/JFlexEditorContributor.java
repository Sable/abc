package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.editor.JastAddEditorContributor;

public class JFlexEditorContributor extends JastAddEditorContributor {

	@Override
	protected String getEditorContextID() {
		return JFlexEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	protected String getEditorID() {
		return JFlexEditor.EDITOR_ID;
	}

	@Override
	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
	}

}
