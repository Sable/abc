package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

public class Semicolon extends Delimiter {
	public Semicolon(LexicalNode previous, Interval interval) {
		super(previous, interval, ";");
	}
	public String toString() {
		return "SEMICOLON\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new Semicolon(previous, getInterval().clone());
	}
	public static final char[] TOKEN = {';'};
}
