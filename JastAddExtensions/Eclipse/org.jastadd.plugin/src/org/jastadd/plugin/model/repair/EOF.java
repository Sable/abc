package org.jastadd.plugin.model.repair;

public class EOF extends Island {
	public EOF(LexicalNode previous, Interval interval) {
		super(previous, interval, "");
	}
	public boolean bridgeMatch(Island island) {
		return island instanceof SOF;
	}
	public Bridge buildBridge(Island target) {
		if (!hasBridge() && bridgeMatch(target)) {
			bridge = new FileBridge((SOF)target, this);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "EOF\t" + super.toString();
	}
	public boolean startOfBridge() {
		return false;
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		return node.getPrevious() == null;
	}
	public Bridge constructIslandAndBridge(LexicalNode node) {
		if (!hasBridge()) {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new SOF(interval);
			Recovery.insertBefore(island, node);
			return buildBridge(island);
		}
		return bridge;
	}
	public LexicalNode clone(LexicalNode previous) {
		return new EOF(previous, getInterval().clone());
	}
}
