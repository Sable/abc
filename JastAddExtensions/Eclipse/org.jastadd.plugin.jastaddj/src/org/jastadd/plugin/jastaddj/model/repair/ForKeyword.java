package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

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
