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
	private static int[] sizeStats;
	private static boolean tabStepDeduced;
	private static int tabStep = TAB_SIZE; // Default
	private static boolean tabsUsed;
	
	public static void clearStats() {
		stats = null;
		sizeStats = null;
		stats = new int[maxIndentStatSize + 1][2][maxIndentStatSize + 1];
		sizeStats = new int[maxIndentStatSize + 1];
		tabStepDeduced = false;
		tabStep = TAB_SIZE; // Default
		tabsUsed = true;
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
		if (tabs > 0) {
			tabsUsed = true;
		}
		sizeStats[size]++;
	}
	
	private static void deduceTabStep() {
		// TODO Finish this method
		if (stats == null) {
			return;
		}
		int prevSize = 0;
		int diffSum = 0;
		int nbrDiffs = 0;
		
		for (int i = 1; i < stats.length; i++) {
			//System.out.println("Indent " + i + ": " + sizeStats[i]);
			if (sizeStats[i] != 0) {
				diffSum += (i - prevSize);
				nbrDiffs++;
				prevSize = i;
			}
		}
		tabStep = diffSum/nbrDiffs;
		tabStepDeduced = true;
	}

	public String toString() {
		return "Indent\t" + super.toString();
	}

	public static final char NEWLINE = '\n';
	public static final char WHITESPACE = ' ';
	public static final char TAB = '\t';

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

	public static String getTabStep() {
		if (!tabStepDeduced)
			deduceTabStep();
		String step = "";
		int stepRemain = tabStep;
		
		while (tabsUsed && stepRemain-- / TAB_SIZE > 0) {
			step += TAB;
		}
		
		while (stepRemain-- > 0) {
			step += WHITESPACE;
		}
		return step;
	}
}
