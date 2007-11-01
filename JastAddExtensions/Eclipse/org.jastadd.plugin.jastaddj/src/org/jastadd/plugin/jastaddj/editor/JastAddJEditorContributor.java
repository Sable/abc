package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.ui.IActionBars;
import org.jastadd.plugin.editor.JastAddEditorContributor;

public class JastAddJEditorContributor extends JastAddEditorContributor {
	protected String getEditorID() {
		return JastAddJEditor.EDITOR_ID;
	}
}
