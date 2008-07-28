package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Reef;

public class Indent extends Reef {
	private static final int TAB_SIZE = 4; // Eclipse default size 
	private int nbrWS;
	private int nbrTabs;
	private int size;
	public Indent(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			switch(chars[i]) {
				case WHITESPACE  : nbrWS++; break;
				case TAB : nbrTabs++; break;
			}
		}
		size = nbrTabs*TAB_SIZE + nbrWS;
		collectStats(nbrWS, nbrTabs, size);
	}
	
	private static int maxIndentStatSize = 10;
	private static int[][][] stats;
	private static boolean tabStepDeduced;
	private static int tabStep = TAB_SIZE; // Default
	
	public static void clearStats() {
		stats = null;
		stats = new int[maxIndentStatSize + 1][2][maxIndentStatSize + 1];
		tabStepDeduced = false;
		tabStep = TAB_SIZE; // Default
	}
	
	private static void collectStats(int ws, int tabs, int size) {
		if (size > maxIndentStatSize || stats == null) {
			return;
		}
		if (stats[size] == null) {
			stats[size] = new int[2][maxIndentStatSize + 1];
			stats[size][0] = new int[maxIndentStatSize + 1];
			stats[size][1] = new int[maxIndentStatSize + 1];
		}
		stats[size][0][ws]++;
		stats[size][1][tabs]++;
	}
	
	private static void deduceTabStep() {
		if (stats == null) {
			return;
		}
		int minStep = 0;
		for (;stats.length < minStep && stats[minStep] == null; minStep++);
		if (minStep > stats.length) {
			return;
		}
		tabStep = minStep;
		/*
		int maxWS = 0;
		for (int i = 0; i < stats[minStep][0].length; i++) {
			if (maxWS < stats[minStep][0][i]) {
				maxWS = stats[minStep][0][i];
			}
		}*/
	}

	public String toString() {
		return "Indent\t" + super.toString();
	}

	public static final char NEWLINE = '\n';
	public static final char WHITESPACE = ' ';
	public static final char TAB = '\t';

	/*
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
*/
	public boolean equalTo(Indent indent) {
		/* DEBUG System.out.println("\t\tEqual? Indent: " + this +" and Indent2: " + indent);  */
		/*
		int nbr = nbrWS + nbrTabs*tabStep;
		int indentNbr = indent.nbrWS + indent.nbrTabs*tabStep;
		int diff = Math.abs(nbr-indentNbr);
		return diff <= (tabStep/2);
		*/
	//	return indent.nbrWS == nbrWS && indent.nbrTabs == nbrTabs;
		return indent.size == size;
	}

	public boolean lessThan(Indent indent, int dist) {
		//int d = Math.abs(nbrTabs - indent.nbrTabs);
		//return nbrTabs < indent.nbrTabs && d <= dist;
		if (!tabStepDeduced) {
			deduceTabStep();
		}
		int d = Math.abs(size - indent.size);
		return size < indent.size && d <= (dist*tabStep);
	}

	public boolean lessThan(Indent indent) {
		/*
		int nbr = nbrWS + nbrTabs*tabStep;
		int indentNbr = indent.nbrWS + indent.nbrTabs*tabStep;
		int diff = nbr - indentNbr;
		return diff < -(tabStep/2);
		*/
		//return nbrTabs < indent.nbrTabs || nbrWS < indent.nbrWS;
		return size < indent.size;
	}

	public LexicalNode clone(LexicalNode previous) {
		return new Indent(previous, getInterval().clone(), getValue());
	}
}
