package org.jastadd.plugin.jastaddj.compiler.recovery;

import java.util.ArrayList;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

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

		return JavaKeyword.match(content, start, buf, nodeList, lastMatch);
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
