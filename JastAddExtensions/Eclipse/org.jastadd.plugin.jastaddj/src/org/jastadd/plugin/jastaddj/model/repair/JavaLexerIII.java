package org.jastadd.plugin.jastaddj.model.repair;

import java.util.ArrayList;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

public class JavaLexerIII extends JavaLexerII {

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
				(length = matchDelimiter(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchKeyword(content, start, buf, nodeList, lastMatch)) > 0 ||
				(length = matchIndent(content, start, buf, nodeList, lastMatch)) > 0) {
				return length;
			}
			return 0;
	}

	protected int matchKeyword(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		int length = 0;
		if ((length = matchIfKeyword(content, start, buf, nodeList, lastMatch)) > 0 ||
			(length = matchForKeyword(content, start, buf, nodeList, lastMatch)) > 0) {
			return length;
		}
		return length;
	}

	protected int matchIfKeyword(char[] content, int start, StringBuffer buf, 
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

	protected int matchForKeyword(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		for (int i = 0; i < content.length && i < ForKeyword.TOKEN.length; i++) {
			if (content[start+i] != ForKeyword.TOKEN[i]) {
				return 0;
			}
		}

		createWater(buf, lastMatch, start, nodeList);

		int length = ForKeyword.TOKEN.length;
		Interval interval = new Interval(start, start + length);
		String value = buf.substring(start, start + length);
		/* DEBUG System.out.println("-- match Keyword for = " + value.replace("\n", "NEWLINE")); */
		nodeList.add(new ForKeyword(nodeList.get(nodeList.size()-1), interval, value));
		return length;
	}

	protected int matchLeftParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchLeftParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < LeftParenIII.TOKEN.length; i++) {
			if (start+i >= content.length || 
				LeftParenIII.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + LeftParenIII.TOKEN.length);
		String value = new String(LeftParenIII.TOKEN);
		/* DEBUG System.out.println("-- match LeftParan = " + value); */
		nodeList.add(new LeftParenIII(nodeList.get(nodeList.size()-1), interval));
		return LeftParenIII.TOKEN.length;
	}

	protected int matchRightParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchRightParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < RightParenIII.TOKEN.length; i++) {
			if (start+i >= content.length || 
				RightParenIII.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + RightParenIII.TOKEN.length);
		String value = new String(RightParenIII.TOKEN);
		/* DEBUG System.out.println("-- match RightParanIII = " + value); */
		nodeList.add(new RightParenIII(nodeList.get(nodeList.size()-1), interval));
		return RightParenIII.TOKEN.length;
	}
}
