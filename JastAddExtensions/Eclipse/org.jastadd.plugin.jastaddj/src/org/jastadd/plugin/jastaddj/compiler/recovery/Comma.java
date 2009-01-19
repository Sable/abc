package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

public class Comma extends Delimiter {
	public Comma(LexicalNode previous, Interval interval) {
		super(previous, interval, ",");
	}
	public String toString() {
		return "COMMA\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new Comma(previous, getInterval().clone());
	}
	public static final char[] TOKEN = {','};
}
