package org.jastadd.plugin.compiler.recovery;

public class EOF extends Island {
	public EOF(LexicalNode previous, Interval interval) {
		super(previous, interval, "");
	}
	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof SOF;
	}
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge() && bridgeMatch(target, tol)) {
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
	public Island constructFakeIsland(LexicalNode node, boolean instervalEnd) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
		return new SOF(interval);
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertBefore(island, node);
	}
	public LexicalNode clone(LexicalNode previous) {
		return new EOF(previous, getInterval().clone());
	}
}
