package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

public class Dot extends Delimiter {
	public Dot(LexicalNode previous, Interval interval) {
		super(previous, interval, ".");
	}
	public String toString() {
		return "DOT\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new Dot(previous, getInterval().clone());
	}
	public static final char[] TOKEN = {'.'};
}
