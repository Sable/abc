package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public class LeftParan extends Island {
	public LeftParan(LexicalNode previous, Interval interval) {
		super(previous, interval, "(");
	}

	public boolean bridgeMatch(Island island) {
		return island instanceof RightParan &&
			((RightParan)island).indent().equalTo(indent());
	}	
	public Bridge buildBridge(Island target) {
		if (!hasBridge()) {
			bridge = new ParanBridge(this, (RightParan)target);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "LParen\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new LeftParan(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		return node instanceof Water;
	}
	public Bridge constructIslandAndBridge(LexicalNode node) {
		if (!hasBridge()) {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getEnd(), nodeInterval.getEnd());
			Island island = new RightParan(null, interval);
			island.setFake(true);
			if (node instanceof Water) {
				Recovery.insertAfter(island, node);
			} else { 
				Recovery.insertBefore(island, node);
			}
			return buildBridge(island);
		}
		return bridge;
	}
	public boolean startOfBridge() {
		return true;
	}


	public static final char[] TOKEN = {'('};
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
