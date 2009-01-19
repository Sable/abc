package org.jastadd.plugin.compiler.recovery;

public class Water extends LexicalNode {
	public Water(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
	public String toString() {
		return "Water\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new Water(previous, getInterval(), getValue());
	}
}
