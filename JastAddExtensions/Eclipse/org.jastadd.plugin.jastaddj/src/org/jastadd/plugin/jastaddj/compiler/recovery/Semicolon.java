package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

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
