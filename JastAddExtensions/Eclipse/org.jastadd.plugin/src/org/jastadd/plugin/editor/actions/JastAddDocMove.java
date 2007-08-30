package org.jastadd.plugin.editor.actions;

import org.jastadd.plugin.EditorTools;

public class JastAddDocMove extends JastAddDocAction {
    private int newOffset;
	
	public JastAddDocMove(int newOffset) {
		this.newOffset = newOffset;
	}

	public void perform() {
		EditorTools.setActiveEditorPosition(newOffset);
 	}
}
