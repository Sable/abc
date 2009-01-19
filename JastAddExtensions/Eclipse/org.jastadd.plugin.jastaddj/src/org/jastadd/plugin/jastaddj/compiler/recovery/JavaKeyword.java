package org.jastadd.plugin.jastaddj.compiler.recovery;

import java.util.ArrayList;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.Reef;
import org.jastadd.plugin.compiler.recovery.SOF;
import org.jastadd.plugin.compiler.recovery.Water;

public class JavaKeyword extends Reef {
	public JavaKeyword(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}

	private static final String[] keyword = {
		"if",
		"for",
		"while",
		"do",
		"void",
		"public",
		"private",
		"protected"};
	
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
			// And types
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
	
	public static int match(char[] content, int start, StringBuffer buf,
			ArrayList<LexicalNode> nodeList, int lastMatch) {
		
		boolean match = false;
		int word = 0;
		int length = 0;
		for (; word < keyword.length; word++) {
			int j = 0;
			for (; (start + j) < content.length && j < keyword[word].length(); j++) {
				if (keyword[word].charAt(j) != content[start + j]) {
					break;
				}
			}
			if (j == keyword[word].length()) {
				match = true;
				break;
			}
		}
		
		if (!match)
			return 0;
		
		createWater(buf, lastMatch, start, nodeList);

		length = keyword[word].length();
		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("-- match Java Keyword = " + value.replace("\n", "NEWLINE")); */
		nodeList.add(new JavaKeyword(nodeList.get(nodeList.size()-1), interval, value));
		return length;
	}
		
	protected static void createWater(StringBuffer buf, int start, int end, ArrayList<LexicalNode> nodeList) {
		/* DEBUG System.out.println("createWater, start = " + start + ", end = " + end); */
		if (end - start <= 0) {
			return;
		}

		String water = buf.substring(start, end);
		/* DEBUG System.out.println("-- water = " + water.replace("\n", "NEWLINE")); */
		nodeList.add(new Water(nodeList.get(nodeList.size()-1), new Interval(start, end), water));
	}


	@Override
	public LexicalNode clone(LexicalNode previous) {
		return new JavaKeyword(previous, getInterval().clone(), getValue());
	}
	
	private Indent indent;

	public Indent indent() {
		if (indent == null) {
			LexicalNode node = getPrevious().getPreviousOfType(Indent.class);
			if (node != null) {
				indent = (Indent)node;
			} else {
				SOF sof = (SOF)getPrevious().getPreviousOfType(SOF.class);
				indent = new Indent(null, sof.getInterval(), "", false);
				Recovery.insertAfter(indent, sof);
			}
		}
		return indent;
	}
}
