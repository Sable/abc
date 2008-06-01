package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.*;

public class RightParan extends Island {
	public RightParan(LexicalNode previous, Interval interval) {
		super(previous, interval, ")");
	}

	public boolean bridgeMatch(Island island) {
		return island instanceof LeftParan &&
			((LeftParan)island).indent().equalTo(indent());
	}	
	public Bridge buildBridge(Island target) {
		if (!hasBridge()) {
			bridge = new ParanBridge((LeftParan)target, this);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "RParan\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new RightParan(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		return true;
	}
	public Bridge constructIslandAndBridge(LexicalNode node) {
		if (!hasBridge()) {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new LeftParan(null, interval);
			island.setFake(true);
			if (node instanceof Water) {
				Recovery.insertBefore(island, node);
			} else {
				Recovery.insertAfter(island, node);
			}
			return buildBridge(island);
		}
		return bridge;
	}
	public boolean startOfBridge() {
		return false;
	}


	public static final char[] TOKEN = {')'};
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
