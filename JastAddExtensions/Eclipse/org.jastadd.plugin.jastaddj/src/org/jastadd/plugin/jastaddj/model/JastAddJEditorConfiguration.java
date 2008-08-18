package org.jastadd.plugin.jastaddj.model;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.jastadd.plugin.jastaddj.completion.JastAddJCompletionProcessor;
import org.jastadd.plugin.jastaddj.model.repair.Indent;
import org.jastadd.plugin.jastaddj.model.repair.JavaKeyword;
import org.jastadd.plugin.jastaddj.model.repair.LeftBrace;
import org.jastadd.plugin.jastaddj.model.repair.LeftParen;
import org.jastadd.plugin.jastaddj.model.repair.RightBrace;
import org.jastadd.plugin.jastaddj.model.repair.RightParen;
import org.jastadd.plugin.model.JastAddEditorConfiguration;
import org.jastadd.plugin.model.repair.Island;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Recovery;
import org.jastadd.plugin.model.repair.SOF;

public class JastAddJEditorConfiguration extends JastAddEditorConfiguration {

	public JastAddJEditorConfiguration(JastAddJModel model) {
		this.model = model;
	}

	
	@Override
	public void getDocInsertionAfterNewline(IDocument doc, DocumentCommand cmd) {
		StringBuffer buf = new StringBuffer(doc.get());
		SOF sof = model.getRecoveryLexer().parse(buf);
		LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, cmd.offset);
		
		// Default
		String indent = Indent.getTabStep();
		LexicalNode prev = recoveryNode.getPrevious();
		while (!(prev instanceof SOF)) {
			if (prev instanceof Island || prev instanceof Indent) {
				break;
			}
			prev = prev.getPrevious();
		}
		if(prev instanceof SOF) {
			indent = "\n" + Indent.getTabStep();
			// Insert newline + step
		} else if (prev instanceof LeftBrace) {
			// If the previous island is a RightParen locate previous JavaKeyword and use its indent
			// Default is to use the indent of LeftBrace
			Indent indentNode = ((LeftBrace)prev).indent();
			prev = recoveryNode.getPrevious();
			while (!(prev instanceof SOF)) {
				if (prev instanceof RightParen || prev instanceof Indent) {
					break;
				}
				prev = prev.getPrevious();
			}
			if (prev instanceof RightParen) {
				prev = recoveryNode.getPrevious();
				while (!(prev instanceof SOF)) {
					if (prev instanceof JavaKeyword) {
						break;
					}
					prev = prev.getPrevious();
				}	
				if (prev instanceof JavaKeyword) {
					indentNode = ((JavaKeyword)prev).indent();
				}
			}
			indent = indentNode.getValue() + Indent.getTabStep();
		} else if (prev instanceof LeftParen) {
			// Insert newline + left paren indent + step x 2
			Indent indentNode = ((LeftParen)prev).indent();
			indent = indentNode.getValue() + Indent.getTabStep() + Indent.getTabStep();
		} else if (prev instanceof Indent) {
			Indent indentNode = (Indent)prev;
			indent = indentNode.getValue();
			// Check if previous island is of type LeftParen in case double step should be inserted
			// Insert newline + previous indent
		} else {
			prev = recoveryNode.getPrevious();
			while (!(prev instanceof SOF)) {
				if (prev instanceof Indent) {
					break;
				}
				prev = prev.getPrevious();
			}
			if (prev instanceof Indent)
				indent = ((Indent)prev).getValue();
		}

		cmd.caretOffset = cmd.offset + indent.length();
		cmd.shiftsCaret = false;
		if (!indent.startsWith("\n"))
			indent = "\n" + indent;
		cmd.text = indent;

		/*
		Indent nodeIndent = (Indent)recoveryNode.getPrevious().getPreviousOfType(Indent.class);
		String indentValue = nodeIndent.getValue();
		// Check if the previous (distance 2) is a left brace
		LexicalNode nodeBrace = recoveryNode.getPreviousOfType(LeftBrace.class, 2);
		if (nodeBrace != null) {
			LeftBrace lBrace = (LeftBrace)nodeBrace;
			String lBraceIndent = lBrace.indent().getValue();
			String posIndent = lBraceIndent + Indent.getTabStep();
			cmd.caretOffset = cmd.offset + posIndent.length();
			cmd.shiftsCaret = false;
			// Check if the left brace matches the next right brace
			LexicalNode endBrace = recoveryNode.getNextOfType(RightBrace.class);
			if (endBrace != null) {
				RightBrace rBrace = (RightBrace)endBrace;
				if (!lBrace.indent().equalTo(rBrace.indent())) {
					// Insert incremented indent plus right brace
					cmd.text = posIndent + lBraceIndent + RightBrace.TOKEN[0];
					return;
				}
			} 
			// Insert incremented indent
			cmd.text = posIndent;
			return;
		}
		// Check if the previous island is a left paren
		LexicalNode island = Recovery.previousIsland(recoveryNode);
		if (island instanceof LeftParen) {
			LeftParen lParen = (LeftParen)island;
			String lParenIndent = lParen.indent().getValue();
			String posIndent = lParenIndent + Indent.getTabStep() + Indent.getTabStep();
			cmd.caretOffset = cmd.offset + posIndent.length();
			cmd.shiftsCaret = false;
			cmd.text = posIndent;
			return;
		}
		// Insert indent
		cmd.caretOffset = cmd.offset + indentValue.length();
		cmd.shiftsCaret = false;
		cmd.text = indentValue;
		*/
	}
	

	@Override
	public void getDocInsertionOnKeypress(IDocument doc, DocumentCommand cmd) {
		char c = cmd.text.charAt(0);
		String content = doc.get();
		int previousKeypressOffset = cmd.offset - 1;
		char previousKeypress = 0;
		if (content.length() > 0 && previousKeypressOffset > 0) {
			previousKeypress = content.charAt(previousKeypressOffset);
		}
		if (LeftParen.TOKEN[0] == c) { 
			cmd.caretOffset = cmd.offset + 1;
			cmd.shiftsCaret = false;
			cmd.text += String.valueOf(RightParen.TOKEN[0]); 
		} else if ('[' == c) {
			cmd.caretOffset = cmd.offset + 1;
			cmd.shiftsCaret = false;
			cmd.text += "]";
		} else if (RightParen.TOKEN[0] == c
				&& previousKeypress == LeftParen.TOKEN[0]) {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if (']' == c && previousKeypress == '[') {
			cmd.text = "";
			cmd.caretOffset = cmd.offset + 1;
		} else if ('"' == c) {
			if (previousKeypress != '"') {
				cmd.caretOffset = cmd.offset + 1;
				cmd.shiftsCaret = false;
				cmd.text += '"';
			} else {
				cmd.text = "";
				cmd.caretOffset = cmd.offset + 1;
			}
		} else if (RightBrace.TOKEN[0] == c) {
			/*
			StringBuffer buf = new StringBuffer(doc.get());
			SOF sof = model.getRecoveryLexer().parse(buf);
			LexicalNode recoveryNode = Recovery.findNodeForOffset(sof, cmd.offset);
			Indent nodeIndent = (Indent)recoveryNode.getPrevious().getPreviousOfType(Indent.class);
			String indentValue = nodeIndent.getValue();
			// Check if the previous (distance 2) is a left brace
			LexicalNode nodeBrace = recoveryNode.getPreviousOfType(LeftBrace.class);
			if (nodeBrace != null) {
				Indent lIndent = ((LeftBrace) nodeBrace).indent();
				String lIndentValue = lIndent.getValue();
			}
			*/
		}
		previousKeypress = c;		
	}

	/*
	@Override
	public ITokenScanner getScanner() {
		return new JastAddJScanner(new JastAddColors());
	}
	*/

	private IContentAssistProcessor completionProcessor;
	
	@Override
	public IContentAssistProcessor getCompletionProcessor() {
		if (completionProcessor == null) {
			completionProcessor = new JastAddJCompletionProcessor();
		}
		return completionProcessor;
	}
}
