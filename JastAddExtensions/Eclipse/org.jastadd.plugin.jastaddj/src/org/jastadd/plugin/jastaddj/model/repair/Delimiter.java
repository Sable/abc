package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Reef;

public abstract class Delimiter extends Reef {
	public Delimiter(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
}
