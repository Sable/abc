package org.jastadd.plugin.jastaddj.editor.highlight;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
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

public class JastAddJScanner extends RuleBasedScanner {
	
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

	public JastAddJScanner(ColorRegistry colors) {
		this.colors = colors;
		registerRules();
	}

	protected ColorRegistry colors;

	protected Token defaultToken;
	protected Token keywordToken;
	protected Token stringToken;
	protected Token commentToken;

	protected WordRule words;
	protected List<IRule> rules;

	protected void registerRules() {

		createTokens();
		
		rules = new ArrayList<IRule>();
		words = new WordRule(new JastAddWordDetector(), defaultToken);
		
		addRules();
		registerWords();
		
		rules.add(words);

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		setDefaultReturnToken(defaultToken);
	}

	protected void createTokens() {
		this.keywordToken= new Token(new TextAttribute(colors.get(new RGB(0x7f, 0x00, 0x55)), 
				colors.get(new RGB(0xff, 0xff, 0xff)), SWT.BOLD));
		this.stringToken = new Token(new TextAttribute(colors.get(new RGB(0x2a, 0x00, 0xff))));
		this.commentToken = new Token(new TextAttribute(colors.get(new RGB(0x3f, 0x7f, 0x5f))));
		this.defaultToken = new Token(new TextAttribute(colors.get(new RGB(0,0,0))));		
	}
	
	protected void addRules() {
		rules.add(new MultiLineRule("/*", "*/", commentToken));
		rules.add(new EndOfLineRule("//", commentToken));
		rules.add(new WhitespaceRule(new JastAddWhitespaceDetector()));
		rules.add(new SingleLineRule("'", "'", stringToken, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("\"", "\"", stringToken, '\\'));		
	}

	protected void registerWords() {
		for(int i = 0; i < keywords.length; i++) {
			words.addWord(keywords[i], keywordToken);
		}
		for(int i = 0; i < types.length; i++) {
			words.addWord(types[i], keywordToken);
		}
		for(int i = 0; i < constants.length; i++) {
			words.addWord(constants[i], keywordToken);
		}
	}

	private String[] keywords = { 
			"abstract", //$NON-NLS-1$
			"break", //$NON-NLS-1$
			"case", "catch", "class", "const", "continue", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"default", "do", //$NON-NLS-2$ //$NON-NLS-1$
			"else", "extends", //$NON-NLS-2$ //$NON-NLS-1$
			"final", "finally", "for", //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"goto", //$NON-NLS-1$
			"if", "implements", "import", "instanceof", "interface", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"native", "new", //$NON-NLS-2$ //$NON-NLS-1$
			"package", "private", "protected", "public", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"return", //$NON-NLS-1$
			"static", "super", "switch", "synchronized", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"this", "throw", "throws", "transient", "try", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			"volatile", //$NON-NLS-1$
			"while", //$NON-NLS-1$
	};

	private String[] types = { 
			"void", 
			"boolean", 
			"char", 
			"byte", 
			"short", 
			"strictfp", 
			"int", 
			"long", 
			"float", 
			"double"
	};

	private String[] constants = {
			"null",
			"true",
			"false"
	};

}
