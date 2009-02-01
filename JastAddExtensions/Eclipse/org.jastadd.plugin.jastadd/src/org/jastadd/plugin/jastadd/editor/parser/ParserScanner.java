package org.jastadd.plugin.jastadd.editor.parser;

import org.eclipse.jface.text.rules.MultiLineRule;
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;
import org.jastadd.plugin.util.ColorRegistry;

public class ParserScanner extends JastAddJScanner {
	
	public ParserScanner(ColorRegistry colors) {
		super(colors);
	}

	protected void addRules() {
		rules.add(new MultiLineRule("{:", ":}", commentToken));
	}
/*
	protected Token codeToken;
	
	protected void createTokens() {
		super.createTokens();
		this.codeToken = new Token(new TextAttribute(colors.get(new RGB(0xfd, 0x96, 0x04))));
	}
*/
}
