package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Reef;

public abstract class Delimiter extends Reef {
	public Delimiter(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
}
