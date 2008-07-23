package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

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
