package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

public class ForKeyword extends JavaKeyword {
	public ForKeyword(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
	public String toString() {
		return "FOR\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new ForKeyword(previous, getInterval().clone(), getValue());
	}
	public static final char[] TOKEN = {'f', 'o', 'r'};
}
