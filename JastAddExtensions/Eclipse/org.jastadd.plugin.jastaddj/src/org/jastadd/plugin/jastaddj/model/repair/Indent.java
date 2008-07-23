package org.jastadd.plugin.jastaddj.model.repair;

import java.util.*;

import org.jastadd.plugin.model.repair.EOF;
import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Reef;
import org.jastadd.plugin.model.repair.SOF;

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

	private static int tabStep = 4;

	private class IndentList {
		ArrayList<Indent> list = new ArrayList<Indent>();
		public void insert(Indent indent) {
			for (int i = 0; i < list.size(); i++) {
				Indent ind = list.get(i);
				if (ind.nbrTabs > indent.nbrTabs) {
					// Insert before
					list.add(i, indent);
					return;
				} else if (ind.nbrTabs == indent.nbrTabs) {
					if (ind.nbrWS > indent.nbrWS) {
						// Insert before
						list.add(i, indent);
						return;
					} else if (ind.nbrWS == indent.nbrWS) {
						// Don't add an identical indent
						return;
					}
				}
			}
			// Insert last
			list.add(indent);
		}
		public int averageTabStep() {
			if (list.size() > 1) {
				int[] step = new int[list.size()];
				Indent prev = list.get(0);	
				for (int i = 1; i < list.size(); i++) {
					Indent indent = list.get(i);
					int tabDiff = indent.nbrTabs - prev.nbrTabs;
					int wsDiff = indent.nbrWS - prev.nbrWS;
					// Do something with the step difference
				}	
			}
			return tabStep;
		}
	}

	public void collectTabInfo(SOF sof) {
		IndentList list = new IndentList();
		LexicalNode node = sof.getNext();
		while (!(node instanceof EOF)) {
			if (node instanceof Indent) 
				list.insert((Indent)node);
			node = node.getNext();
		}
		tabStep = list.averageTabStep();
	}

	public boolean equalTo(Indent indent) {
		/* DEBUG System.out.println("\t\tEqual? Indent: " + this +" and Indent2: " + indent);  */
		/*
		int nbr = nbrWS + nbrTabs*tabStep;
		int indentNbr = indent.nbrWS + indent.nbrTabs*tabStep;
		int diff = Math.abs(nbr-indentNbr);
		return diff <= (tabStep/2);
		*/
		return indent.nbrWS == nbrWS && indent.nbrTabs == nbrTabs;
	}

	public boolean lessThan(Indent indent, int dist) {
		int d = Math.abs(nbrTabs - indent.nbrTabs);
		return nbrTabs < indent.nbrTabs && d <= dist;
	}

	public boolean lessThan(Indent indent) {
		/*
		int nbr = nbrWS + nbrTabs*tabStep;
		int indentNbr = indent.nbrWS + indent.nbrTabs*tabStep;
		int diff = nbr - indentNbr;
		return diff < -(tabStep/2);
		*/
		return nbrTabs < indent.nbrTabs || nbrWS < indent.nbrWS;
	}

	public LexicalNode clone(LexicalNode previous) {
		return new Indent(previous, getInterval().clone(), getValue());
	}
}
