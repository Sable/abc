package org.jastadd.plugin.jastadd.editor.grammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.jastadd.plugin.util.ColorRegistry;

public class ASTScanner extends RuleBasedScanner implements ITokenScanner {
	
	public class JastAddWhitespaceDetector implements IWhitespaceDetector {
		public boolean isWhitespace(char c) {
			return Character.isWhitespace(c);
		}
	}
	
	public class JastAddWordDetector implements IWordDetector {
		public boolean isWordPart(char character) {
			return Character.isJavaIdentifierPart(character);
		}
		public boolean isWordStart(char character) {
			return Character.isJavaIdentifierStart(character);
		}
	}

	public ASTScanner(ColorRegistry colors) {
		this.colors = colors;
		registerRules();
	}

	protected ColorRegistry colors;

	protected Token defaultToken;
	protected Token keywordToken;
	protected Token commentToken;
	private Token stringToken;
	private Token terminalToken;
	
	protected WordRule words;

	protected void registerRules() {

		createTokens();
		
		List<IRule> rules = new ArrayList<IRule>();
		words = new WordRule(new JastAddWordDetector(), defaultToken);
		rules.add(new MultiLineRule("/*", "*/", commentToken));
		rules.add(new EndOfLineRule("//", commentToken));
		rules.add(new WhitespaceRule(new JastAddWhitespaceDetector()));
		rules.add(new SingleLineRule("'", "'", stringToken, '\\'));
		rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));
		rules.add(new SingleLineRule("<", ">", terminalToken, '\\'));

		registerWords();
		
		rules.add(words);
		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		setDefaultReturnToken(defaultToken);
	}

	protected void createTokens() {
		this.keywordToken= new Token(new TextAttribute(
				colors.get(new RGB(0x7f, 0x00, 0x55)),
				colors.get(new RGB(0xff, 0xff, 0xff)),
				SWT.BOLD));
		this.stringToken = new Token(new TextAttribute(colors.get(new RGB(0x2a, 0x00, 0xff))));
		this.commentToken = new Token(new TextAttribute(colors.get(new RGB(0x3f, 0x7f, 0x5f))));
		this.defaultToken = new Token(new TextAttribute(colors.get(new RGB(0x49,0x1d,0x64))));
		this.terminalToken = new Token(new TextAttribute(colors.get(new RGB(0x49,0x1d,0x64)),
				colors.get(new RGB(0xcb,0xbb,0xd5)), SWT.NORMAL));
	}

	protected void registerWords() {
		for(int i = 0; i < keywords.length; i++) {
			words.addWord(keywords[i], keywordToken);
		}
	}

	private String[] keywords = { "abstract", "package", "::=" };
}
