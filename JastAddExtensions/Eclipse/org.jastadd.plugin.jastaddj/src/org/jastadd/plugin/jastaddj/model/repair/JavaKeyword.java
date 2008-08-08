package org.jastadd.plugin.jastaddj.model.repair;

import java.util.ArrayList;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Recovery;
import org.jastadd.plugin.model.repair.Reef;
import org.jastadd.plugin.model.repair.SOF;
import org.jastadd.plugin.model.repair.Water;

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
