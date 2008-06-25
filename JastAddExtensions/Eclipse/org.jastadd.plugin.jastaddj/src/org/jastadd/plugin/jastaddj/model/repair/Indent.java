package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public class Indent extends Reef {
	private int nbrWS;
	private int nbrTabs;
	public Indent(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			switch(chars[i]) {
				case WHITESPACE  : nbrWS++; break;
				case TAB : nbrTabs++; break;
			}
		}
	}
	public String toString() {
		return "Indent\t" + super.toString();
	}

	public static final char NEWLINE = '\n';
	public static final char WHITESPACE = ' ';
	public static final char TAB = '\t';

	public boolean equalTo(Indent indent) {
		/*
		int nbr = nbrWS + nbrTabs*4;
		int indentNbr = indent.nbrWS + indent.nbrTabs*4;
		int diff = Math.abs(nbr-indentNbr);
		return diff <= 2;
		*/
		return indent.nbrWS == nbrWS && indent.nbrTabs == nbrTabs;
	}

	public boolean lessThan(Indent indent) {
		/*
		int nbr = nbrWS + nbrTabs*4;
		int indentNbr = indent.nbrWS + indent.nbrTabs*4;
		int diff = nbr - indentNbr;
		return diff < -2;
		*/
		return nbrTabs < indent.nbrTabs || nbrWS < indent.nbrWS;
	}

	public LexicalNode clone(LexicalNode previous) {
		return new Indent(previous, getInterval().clone(), getValue());
	}
}
