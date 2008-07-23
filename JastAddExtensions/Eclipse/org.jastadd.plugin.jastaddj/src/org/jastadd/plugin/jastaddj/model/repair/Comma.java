package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

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
