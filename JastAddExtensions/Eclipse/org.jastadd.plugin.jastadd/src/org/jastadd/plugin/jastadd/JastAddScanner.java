package org.jastadd.plugin.jastadd;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.jastadd.plugin.editor.highlight.JastAddColors;
import org.jastadd.plugin.jastaddj.editor.highlight.JastAddJScanner;

public class JastAddScanner extends JastAddJScanner implements ITokenScanner {

	protected Token aspectToken;
	
	public JastAddScanner(JastAddColors colors) {
		super(colors);
	}
	
	protected void createTokens() {
		super.createTokens();
		this.aspectToken = new Token(new TextAttribute(colors.get(new RGB(0x2d, 0x66, 0x2d)), 
				colors.get(new RGB(0xff, 0xff, 0xff)), SWT.BOLD));
	}
	
	protected void registerWords() {
		super.registerWords();		
		String[] aspect = { 
				"syn",
				"inh",
				"lazy",
				"aspect",
				"eq",
				"rewrite",
				"when",
				"to",
				"col",
			};
		for(int i = 0; i < aspect.length; i++) {
			words.addWord(aspect[i], aspectToken);
		}
	}

}
