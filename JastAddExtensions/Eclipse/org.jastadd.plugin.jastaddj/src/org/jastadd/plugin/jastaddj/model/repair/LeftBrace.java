package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public class LeftBrace extends Island {
	public LeftBrace(LexicalNode previous, Interval interval) {
		super(previous, interval, "{");
	}

	public boolean bridgeMatch(Island island) {
		return island instanceof RightBrace && 
			((RightBrace)island).indent().equalTo(indent());
	}
	public Bridge buildBridge(Island target) {
		if (!hasBridge()) {
			bridge = new BraceBridge(this, (RightBrace)target);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "LBrace\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new LeftBrace(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		if (node instanceof Indent) {
			Indent targetIndent = (Indent)node;
			if(targetIndent.lessThan(indent())) {
				/* DEBUG System.out.println("\tFound greater targetIndent: " + targetIndent); */
				LexicalNode previous = targetIndent.getPrevious();
				/* DEBUG System.out.println("\tFound previous: " + previous); */
				if (previous != null) {
					Indent prevIndent = (Indent)previous.getPreviousOfType(Indent.class);
					/* DEBUG System.out.println("\tFound prev indent: " + prevIndent); */
					return prevIndent == null || prevIndent.equalTo(indent);
				}
				return false;
			}
		}
		return false;
	}
	public Bridge constructIslandAndBridge(LexicalNode node) {
		if (!hasBridge()) {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new RightBrace(null, interval);
			island.setFake(true);
			Recovery.insertBefore(island, node);
			/* DEBUG System.out.println("\tReady to build bridge to fake island"); */
			return buildBridge(island);
		}
		return bridge;
	}
	public boolean startOfBridge() {
		return true;
	}



	public static final char[] TOKEN = {'{'};
	private Indent indent;

	public Indent indent() {
		if (indent == null) {
			LexicalNode node = getPrevious().getPreviousOfType(Indent.class);
			if (node != null) {
				indent = (Indent)node;
			} else {
				SOF sof = (SOF)getPrevious().getPreviousOfType(SOF.class);
				indent = new Indent(null, sof.getInterval(), "");
				Recovery.insertAfter(indent, sof);
			}
		}
		return indent;
	}
}
