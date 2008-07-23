package org.jastadd.plugin.model.repair;

public abstract class Reef extends LexicalNode {
	public Reef(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
}
