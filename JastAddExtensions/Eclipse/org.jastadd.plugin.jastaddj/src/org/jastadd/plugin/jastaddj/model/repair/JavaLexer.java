package org.jastadd.plugin.jastaddj.model.repair;

import java.io.*;
import java.util.*;

import org.jastadd.plugin.model.repair.*;

public class JavaLexer implements RecoveryLexer {

	public SOF parse(StringBuffer buf) {
		ArrayList<LexicalNode> nodeList = new ArrayList<LexicalNode>();
		char[] content = buf.toString().toCharArray();
		int start = 0;
		int lastMatch = 0;
		int length = 0;
		SOF sof = new SOF(new Interval(0,0));
		nodeList.add(sof);
		while (start < content.length) {
			if ((length = matchJavaWater(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Islands - Braces */
				(length = matchLeftBrace(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchRightBrace(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Islands - Parans */
				(length = matchLeftParan(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchRightParan(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Reefs */
				(length = matchKeyword(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchIndent(content, start, buf, nodeList, lastMatch)) > 0) {
				start += length;
				lastMatch = start;
			} else start++;
		}
		if (start - lastMatch - 1 > 0) {
			createWater(buf, lastMatch + 1, start, nodeList);
		}
		nodeList.add(new EOF(nodeList.get(nodeList.size()-1), 
			new Interval(content.length-1,content.length-1)));
		return sof;
	}

	private void createWater(StringBuffer buf, int start, int end, ArrayList<LexicalNode> nodeList) {
		/* DEBUG System.out.println("createWater, start = " + start + ", end = " + end); */
		if (end - start <= 0) {
			return;
		}

		String water = buf.substring(start, end);
		/* DEBUG System.out.println("-- water = " + water.replace("\n", "NEWLINE")); */
		nodeList.add(new Water(nodeList.get(nodeList.size()-1), new Interval(start, end), water));
	}

	private int matchJavaWater(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchJavaWater, start = " + start + ", lastMatch = " + lastMatch); */

		int length = 0;
		// Comment start
		if (content[start] == JavaWater.COMMENT[0]) {
			length++;
			if (start + length < content.length) {
				// Line comment
				if (content[start+length] == JavaWater.COMMENT[0]) {
				    /* DEBUG System.out.println("-- match line comment"); */
					length+=2;
					// Loop until end of line
					for (int i = start+length; i < content.length; i++) {
						if (content[i] == Indent.NEWLINE) {
							break;
						}
						length++;
					}
				}
				// Multi comment
				else if (content[start+length] == JavaWater.COMMENT[1]) {
					/* DEBUG System.out.println("-- match multi line comment"); */
					length++;
					// Loop until end of multi comment
					for (int i = start+length+1; i < content.length-1; i++) {
						length++;
						if (content[i] == JavaWater.COMMENT[1] && 
							content[i+1] == JavaWater.COMMENT[0]) {
							length+=2;
							break;
						}
					}
				} else {
					return 0;
				}
			}
		}
		// String
		else if (content[start] == JavaWater.STRING) {
			length++;
			for (int i = start + length + 1; i < content.length; i++) {
				length++;
				if (content[i] == JavaWater.STRING) {
					length++;
					break;
				}
			}
		} else {
			return 0;
		}

		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("--match JavaWater = " + value); */
		nodeList.add(new JavaWater(nodeList.get(nodeList.size()-1), interval, value));
		return length;
	}

	private int matchLeftBrace(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchLeftBrace, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < LeftBrace.TOKEN.length; i++) {
			if (start+i >= content.length || 
				LeftBrace.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + LeftBrace.TOKEN.length);
		String value = new String(LeftBrace.TOKEN);
		/* DEBUG System.out.println("-- match LeftBrace = " + value); */
		nodeList.add(new LeftBrace(nodeList.get(nodeList.size()-1), interval));
		return LeftBrace.TOKEN.length;
	}

	private int matchRightBrace(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchRightBrace, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < RightBrace.TOKEN.length; i++) {
			if (start+i >= content.length || 
				RightBrace.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}

		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + RightBrace.TOKEN.length);
		String value = new String(RightBrace.TOKEN);
		/* DEBUG System.out.println("-- match RightBrace = " + value); */
		nodeList.add(new RightBrace(nodeList.get(nodeList.size()-1), interval));
		return RightBrace.TOKEN.length;
	}

	private int matchIndent(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchIndent, start = " + start + ", lastMatch = " + lastMatch); */

		if (content[start] != Indent.NEWLINE) {
			return 0;
		}

		createWater(buf, lastMatch, start, nodeList);

		int length = 1;
		for (int i = start + 1; i < content.length; i++) {
			if (content[i] != Indent.WHITESPACE && content[i] != Indent.TAB) {
				break;
			}
			length++;
		}
		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("-- match Indent = " + value.replace("\n", "NEWLINE")); */
		nodeList.add(new Indent(nodeList.get(nodeList.size()-1), interval, value));
		return length;
	}

	private int matchKeyword(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		int length = 0;
		if ((length = matchIfKeyword(content, start, buf, nodeList, lastMatch)) > 0)
			return length;
		return length;
	}

	private int matchIfKeyword(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		for (int i = 0; i < content.length && i < IfKeyword.TOKEN.length; i++) {
			if (content[start+i] != IfKeyword.TOKEN[i]) {
				return 0;
			}
		}

		createWater(buf, lastMatch, start, nodeList);

		int length = IfKeyword.TOKEN.length;
		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("-- match Keyword if = " + value.replace("\n", "NEWLINE")); */
		nodeList.add(new IfKeyword(nodeList.get(nodeList.size()-1), interval, value));
		return length;
	}

	private int matchLeftParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchLeftParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < LeftParan.TOKEN.length; i++) {
			if (start+i >= content.length || 
				LeftParan.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + LeftParan.TOKEN.length);
		String value = new String(LeftParan.TOKEN);
		/* DEBUG System.out.println("-- match LeftParan = " + value); */
		nodeList.add(new LeftParan(nodeList.get(nodeList.size()-1), interval));
		return LeftParan.TOKEN.length;
	}

	private int matchRightParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchRightParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < RightParan.TOKEN.length; i++) {
			if (start+i >= content.length || 
				RightParan.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + RightParan.TOKEN.length);
		String value = new String(RightParan.TOKEN);
		/* DEBUG System.out.println("-- match RightParan = " + value); */
		nodeList.add(new RightParan(nodeList.get(nodeList.size()-1), interval));
		return RightParan.TOKEN.length;
	}
}
