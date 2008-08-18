package org.jastadd.plugin.jastadd;

import org.jastadd.plugin.editor.JastAddEditorContributor;

public class ASTEditorContributor extends JastAddEditorContributor {

	@Override
	protected String getEditorContextID() {
		return ASTEditor.EDITOR_CONTEXT_ID;
	}

	@Override
	protected String getEditorID() {
		return ASTEditor.EDITOR_ID;
	}

	@Override
	protected void registerStopHandler(Runnable stopHandler) {
		Activator.INSTANCE.addStopHandler(stopHandler);
	}

}
