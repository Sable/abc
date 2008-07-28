package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;

public class StringWater extends JavaWater {

	public StringWater(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
	
	public String toString() {
		return "Str-" + super.toString();
	}

	public LexicalNode clone(LexicalNode previous) {
		return new StringWater(previous, getInterval().clone(), getValue());
	}

	public boolean includeInPrettyPrint() {
		return true;
	}

	public static final char STRING = '"';
}
