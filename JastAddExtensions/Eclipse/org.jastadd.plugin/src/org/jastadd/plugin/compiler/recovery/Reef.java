package org.jastadd.plugin.compiler.recovery;

public abstract class Reef extends LexicalNode {
	public Reef(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
}
