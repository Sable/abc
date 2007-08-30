package org.jastadd.plugin.editor.actions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class JastAddDocReplace extends JastAddDocAction {

	private IDocument doc; 
	private int offset;
	private String replaceText;
	private int replaceLength;
	
	public JastAddDocReplace(IDocument doc, int offset, String replaceText, int replaceLength) {
		this.doc = doc;
		this.offset = offset;
		this.replaceText = replaceText;
		this.replaceLength = replaceLength;
	}

	public void perform() {
		try {
			doc.replace(offset, replaceLength, replaceText);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
