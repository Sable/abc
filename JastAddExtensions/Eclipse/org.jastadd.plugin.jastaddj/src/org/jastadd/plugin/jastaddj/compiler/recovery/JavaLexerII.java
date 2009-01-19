package org.jastadd.plugin.jastaddj.compiler.recovery;

import java.util.ArrayList;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

public class JavaLexerII extends JavaLexer {

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
				(length = matchIndent(content, start, buf, nodeList, lastMatch)) > 0) {
				return length;
			}
			return 0;
	}

	protected int matchDelimiter(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		int length = 0;
		if ((length = matchComma(content, start, buf, nodeList, lastMatch)) > 0 ||
			(length = matchDot(content, start, buf, nodeList, lastMatch)) > 0 ||
			(length = matchSemicolon(content, start, buf, nodeList, lastMatch)) > 0) {
			return length;
		}
		return length;
	}

	protected int matchSemicolon(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchSemicolon, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < Semicolon.TOKEN.length; i++) {
			if (start+i >= content.length || 
				Semicolon.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + Semicolon.TOKEN.length);
		String value = new String(Semicolon.TOKEN);
		/* DEBUG System.out.println("-- match Semicolon = " + value); */
		nodeList.add(new Semicolon(nodeList.get(nodeList.size()-1), interval));
		return Semicolon.TOKEN.length;
	}

	protected int matchDot(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchDot, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < Dot.TOKEN.length; i++) {
			if (start+i >= content.length || 
				Dot.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + Dot.TOKEN.length);
		String value = new String(Dot.TOKEN);
		/* DEBUG System.out.println("-- match Dot = " + value); */
		nodeList.add(new Dot(nodeList.get(nodeList.size()-1), interval));
		return Dot.TOKEN.length;
	}

	protected int matchComma(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchComma, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < Comma.TOKEN.length; i++) {
			if (start+i >= content.length || 
				Comma.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + Comma.TOKEN.length);
		String value = new String(Comma.TOKEN);
		/* DEBUG System.out.println("-- match Comma = " + value); */
		nodeList.add(new Comma(nodeList.get(nodeList.size()-1), interval));
		return Comma.TOKEN.length;
	}

	protected int matchLeftParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchLeftParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < LeftParenII.TOKEN.length; i++) {
			if (start+i >= content.length || 
				LeftParenII.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + LeftParenII.TOKEN.length);
		String value = new String(LeftParenII.TOKEN);
		/* DEBUG System.out.println("-- match LeftParanI = " + value); */
		nodeList.add(new LeftParenII(nodeList.get(nodeList.size()-1), interval));
		return LeftParenII.TOKEN.length;
	}

	protected int matchRightParan(char[] content, int start, StringBuffer buf, 
				ArrayList<LexicalNode> nodeList, int lastMatch) {
		/* DEBUG System.out.println("[" + (content[start]=='\n'?"NEWLINE":content[start]) + 
			"] matchRightParan, start = " + start + ", lastMatch = " + lastMatch); */

		for (int i = 0; i < RightParenII.TOKEN.length; i++) {
			if (start+i >= content.length || 
				RightParenII.TOKEN[i] != content[start+i]) {
				return 0;
			}
		}
	
		createWater(buf, lastMatch, start, nodeList);

		Interval interval = new Interval(start, start + RightParenII.TOKEN.length);
		String value = new String(RightParenII.TOKEN);
		/* DEBUG System.out.println("-- match RightParanI = " + value); */
		nodeList.add(new RightParenII(nodeList.get(nodeList.size()-1), interval));
		return RightParenII.TOKEN.length;
	}
}
