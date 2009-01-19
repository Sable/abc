package org.jastadd.plugin.jastaddj.compiler.recovery;

import java.util.ArrayList;

import org.jastadd.plugin.compiler.recovery.EOF;
import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.RecoveryLexer;
import org.jastadd.plugin.compiler.recovery.SOF;
import org.jastadd.plugin.compiler.recovery.Water;

public class JavaLexer implements RecoveryLexer {

	public SOF parse(StringBuffer buf) {
		Indent.clearStats();
		ArrayList<LexicalNode> nodeList = new ArrayList<LexicalNode>();
		char[] content = buf.toString().toCharArray();
		int start = 0;
		int lastMatch = 0;
		int length = 0;
		SOF sof = new SOF(new Interval(0,0));
		nodeList.add(sof);
		while (start < content.length) {
			if ((length = match(content, start, buf, nodeList, lastMatch)) > 0) {
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

	protected int match(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
			int length = 0;
			if ((length = matchJavaWater(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Islands - Braces */
				(length = matchLeftBrace(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchRightBrace(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Islands - Parans */
				(length = matchLeftParan(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchRightParan(content, start, buf, nodeList, lastMatch)) > 0 ||
				/* Reefs */
				(length = matchIndent(content, start, buf, nodeList, lastMatch)) > 0) {
				return length;
			}
			return 0;
	}

	protected void createWater(StringBuffer buf, int start, int end, ArrayList<LexicalNode> nodeList) {
		/* DEBUG System.out.println("createWater, start = " + start + ", end = " + end); */
		if (end - start <= 0) {
			return;
		}

		String water = buf.substring(start, end);
		/* DEBUG System.out.println("-- water = " + water.replace("\n", "NEWLINE")); */
		nodeList.add(new Water(nodeList.get(nodeList.size()-1), new Interval(start, end), water));
	}

	protected int matchJavaWater(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchJavaWater, start = " + start + ", lastMatch = " + lastMatch); */
		boolean stringMatch = false;
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
		else if (content[start] == StringWater.STRING) {
			length++;
			for (int i = start + 1; i < content.length; i++) {
				length++;
				if (content[i] == StringWater.STRING) {
					stringMatch = true;
					//length++;
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
		if (stringMatch) {
			nodeList.add(new StringWater(nodeList.get(nodeList.size()-1), interval, value));
		} else {
			nodeList.add(new JavaWater(nodeList.get(nodeList.size()-1), interval, value));
		}
		return length;
	}

	protected int matchLeftBrace(char[] content, int start, StringBuffer buf, 
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

	protected int matchRightBrace(char[] content, int start, StringBuffer buf, 
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

	protected int matchIndent(char[] content, int start, StringBuffer buf, 
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
		boolean emptyLine = false;
		if (start + length < content.length && content[start + length] == Indent.NEWLINE) {
			emptyLine = true;
		}
		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("-- match Indent = " + value.replace("\n", "NEWLINE")); */
		nodeList.add(new Indent(nodeList.get(nodeList.size()-1), interval, value, emptyLine));
		return length;
	}

	protected int matchLeftParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchLeftParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < LeftParen.TOKEN.length; i++) {
			if (start+i >= content.length || 
				LeftParen.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + LeftParen.TOKEN.length);
		String value = new String(LeftParen.TOKEN);
		/* DEBUG System.out.println("-- match LeftParan = " + value); */
		nodeList.add(new LeftParen(nodeList.get(nodeList.size()-1), interval));
		return LeftParen.TOKEN.length;
	}

	protected int matchRightParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchRightParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < RightParen.TOKEN.length; i++) {
			if (start+i >= content.length || 
				RightParen.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + RightParen.TOKEN.length);
		String value = new String(RightParen.TOKEN);
		/* DEBUG System.out.println("-- match RightParan = " + value); */
		nodeList.add(new RightParen(nodeList.get(nodeList.size()-1), interval));
		return RightParen.TOKEN.length;
	}
}
