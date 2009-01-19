package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;

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
