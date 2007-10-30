package org.jastadd.plugin.jastaddj.editor;

import org.eclipse.ui.IActionBars;
import org.jastadd.plugin.editor.JastAddEditorContributor;

public class JastAddJEditorContributor extends JastAddEditorContributor {

	public void init(IActionBars bars) {
		super.init(bars);
		populateCommands(JastAddJEditor.EDITOR_ID);
		populateTopMenu(bars.getMenuManager(), JastAddJEditor.EDITOR_ID);
	}
}
