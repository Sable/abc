package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public abstract class JavaKeyword extends Reef {
	public JavaKeyword(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
	}
}
