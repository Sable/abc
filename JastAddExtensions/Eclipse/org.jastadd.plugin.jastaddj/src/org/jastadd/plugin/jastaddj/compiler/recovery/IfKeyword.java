package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

public class IfKeyword extends JavaKeyword {
	public IfKeyword(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
	public String toString() {
		return "IF\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new IfKeyword(previous, getInterval().clone(), getValue());
	}
	public static final char[] TOKEN = {'i', 'f'};
}
