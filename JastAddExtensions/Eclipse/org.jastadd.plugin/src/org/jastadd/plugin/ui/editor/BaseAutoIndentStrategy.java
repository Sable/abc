package org.jastadd.plugin.ui.editor;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

public class BaseAutoIndentStrategy implements IAutoEditStrategy {
	
	public BaseAutoIndentStrategy() {
	}
	
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
		// cmd.length == 0 - when no text is markes
		// cmd.text - the text to insert
		// cmd.doit - false if nothing should be done??

		if (cmd.doit == false)
			return;

		if (cmd.length == 0 && cmd.text != null && isLineDelimiter(doc, cmd.text))
			smartIndentAfterNewLine(doc, cmd);
		else if (cmd.text.length() == 1)
			smartIndentOnKeypress(doc, cmd);
	}

	
	protected void smartIndentOnKeypress(IDocument doc, DocumentCommand cmd) {
	}

	protected void smartIndentAfterNewLine(IDocument doc, DocumentCommand cmd) {
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}
}