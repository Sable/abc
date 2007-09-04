package org.jastadd.plugin.editor.highlight;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.jastadd.plugin.JastAddModel;

public class JastAddAutoIndentStrategy implements IAutoEditStrategy {
	
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

	
	private void smartIndentOnKeypress(IDocument doc, DocumentCommand cmd) {
		JastAddModel.getInstance().getDocInsertionOnKeypress(doc, cmd);
	}

	private void smartIndentAfterNewLine(IDocument doc, DocumentCommand cmd) {
		JastAddModel.getInstance().getDocInsertionAfterNewline(doc, cmd);
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}
}
