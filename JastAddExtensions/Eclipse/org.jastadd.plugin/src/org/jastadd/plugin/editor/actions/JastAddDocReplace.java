package org.jastadd.plugin.editor.actions;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

public class JastAddDocReplace extends JastAddDocAction {

	private IDocument doc; 
	private String replaceText;
	private int replaceLength;
	private DocumentCommand cmd;
	
	public JastAddDocReplace(IDocument doc, DocumentCommand cmd, String replaceText, int replaceLength) {
		this.doc = doc;
		this.cmd = cmd;
		this.replaceText = replaceText;
		this.replaceLength = replaceLength;
	}

	public void perform() {
		String content = doc.get();
		System.out.println("text=#" + replaceText + "# length=" + replaceLength + " offset=" + cmd.offset);
		cmd.text = replaceText;
	}
}
