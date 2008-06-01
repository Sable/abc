package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public class JavaWater extends Water {
	public JavaWater(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}

	public String toString() {
		return "J" + super.toString();
	}

	public LexicalNode clone(LexicalNode previous) {
		return new JavaWater(previous, getInterval().clone(), getValue());
	}
	
	public boolean includeInPrettyPrint() {
		return false;
	}

	public static final char[] COMMENT = {'/', '*'};
	public static final char STRING = '"';
}
