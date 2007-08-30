package org.jastadd.plugin.editor.highlight;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.editor.actions.JastAddDocAction;

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
		/*
		JastAddDocReplace replace = JastAddModel.getInstance().getDocInsertionAftere(doc, cmd.offset, cmd.text.charAt(0));
		if (replace == null) {
			try {
			  doc.replace(replace.offset, replace.length, replace.text);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		*/
	}

	private void smartIndentAfterNewLine(IDocument doc, DocumentCommand cmd) {
		LinkedList<JastAddDocAction> todoList = JastAddModel.getInstance().getDocInsertionAfterNewline(doc, cmd);
		for (Iterator itr = todoList.iterator();itr.hasNext();) {
			JastAddDocAction action = (JastAddDocAction)itr.next();
			action.perform();
		}
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}

	
	
	/* Old stuff
	// evaluate the line with the opening bracket that matches the closing bracket on the given line
	protected int findMatchingOpenBracket(IDocument d, int line, int end, int closingBracketIncrease) throws BadLocationException {

		int start= d.getLineOffset(line);
		int brackcount= getBracketCount(d, start, end, false) - closingBracketIncrease;

		// sum up the brackets counts of each line (closing brackets count negative, 
		// opening positive) until we find a line the brings the count to zero
		while (brackcount < 0) {
			line--;
			if (line < 0) {
				return -1;
			}
			start= d.getLineOffset(line);
			end= start + d.getLineLength(line) - 1;
			brackcount += getBracketCount(d, start, end, false);
		}
		return line;
	}

	private int getBracketCount(IDocument d, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {

		int bracketcount= 0;
		while (start < end) {
			char curr= d.getChar(start);
			start++;
			switch (curr) {
				case '/' :
					if (start < end) {
						char next= d.getChar(start);
						if (next == '*') {
							// a comment starts, advance to the comment end
							start= getCommentEnd(d, start + 1, end);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line 
							start= end;
						}
					}
					break;
				case '*' :
					if (start < end) {
						char next= d.getChar(start);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							bracketcount= 0;
							start++;
						}
					}
					break;
				case '{' :
					bracketcount++;
					ignoreCloseBrackets= false;
					break;
				case '}' :
					if (!ignoreCloseBrackets) {
						bracketcount--;
					}
					break;
				case '"' :
				case '\'' :
					start= getStringEnd(d, start, end, curr);
					break;
				default :
					}
		}
		return bracketcount;
	}

	// ----------- bracket counting ------------------------------------------------------

	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException {
		while (pos < end) {
			char curr= d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	protected String getIndentOfLine(IDocument d, int line) throws BadLocationException {
		if (line > -1) {
			int start= d.getLineOffset(line);
			int end= start + d.getLineLength(line) - 1;
			int whiteend= findEndOfWhiteSpace(d, start, end);
			return d.get(start, whiteend - start);
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr= d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}

	protected void smartInsertAfterBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p= (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line= d.getLineOfOffset(p);
			int start= d.getLineOffset(line);
			int whiteend= findEndOfWhiteSpace(d, start, c.offset);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int indLine= findMatchingOpenBracket(d, line, c.offset, 1);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText= new StringBuffer(getIndentOfLine(d, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(d.get(whiteend, c.offset - whiteend));
					replaceText.append(c.text);
					// modify document command
					c.length += c.offset - start;
					c.offset= start;
					c.text= replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
		}
	}

	protected void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {

		int docLength= d.getLength();
		if (c.offset == -1 || docLength == 0)
			return;

		try {
			int p= (c.offset == docLength ? c.offset - 1 : c.offset);
			int line= d.getLineOfOffset(p);

			StringBuffer buf= new StringBuffer(c.text);
			if (c.offset < docLength && d.getChar(c.offset) == '}') {
				int indLine= findMatchingOpenBracket(d, line, c.offset, 0);
				if (indLine == -1) {
					indLine= line;
				}
				buf.append(getIndentOfLine(d, indLine));
			} else {
				int start= d.getLineOffset(line);
				// if line just ended a javadoc comment, take the indent from the comment's begin line
				int whiteend= findEndOfWhiteSpace(d, start, c.offset);
				buf.append(d.get(start, whiteend - start));
				if (getBracketCount(d, start, c.offset, true) > 0) {
					buf.append(getOneIndentLevel());
				}
			}
			c.text= buf.toString();

		} catch (BadLocationException excp) {
		}
	}
	
	private String getOneIndentLevel() {
		return String.valueOf('\t');
	}	
	
	private boolean endsWithDelimiter(IDocument d, String txt) {
		
		String[] delimiters= d.getLegalLineDelimiters();
		
		for (int i= 0; i < delimiters.length; i++) {
			if (txt.endsWith(delimiters[i]))
				return true;
		}
		
		return false;
	}	

	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
			smartIndentAfterNewLine(d, c);
		else if ("}".equals(c.text)) { //$NON-NLS-1$
			smartInsertAfterBracket(d, c);
		}
	}
	*/
}
