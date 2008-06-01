package org.jastadd.plugin.model.repair;

public class SOF extends Island {
	public SOF(Interval interval) {
		super(null, interval, "");
	}
	public boolean bridgeMatch(Island island) {
		return island instanceof EOF;
	}
	public Bridge buildBridge(Island target) {
		if (!hasBridge() && bridgeMatch(target)) {
			bridge = new FileBridge(this, (EOF)target);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "SOF\t" + super.toString();
	}
	public boolean startOfBridge() {
		return true;
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		return node.getNext() == null;
	}
	public Bridge constructIslandAndBridge(LexicalNode node) {
		if (!hasBridge()) {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new EOF(node, interval);
			Recovery.insertAfter(island, node);
			return buildBridge(island);
		}
		return bridge;
	}
	public LexicalNode clone(LexicalNode previous) {
		return new SOF(getInterval().clone());
	}
}
