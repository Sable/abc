package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

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
