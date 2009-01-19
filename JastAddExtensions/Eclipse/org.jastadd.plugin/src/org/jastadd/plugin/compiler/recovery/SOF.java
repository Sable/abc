package org.jastadd.plugin.compiler.recovery;

public class SOF extends Island {
	public SOF(Interval interval) {
		super(null, interval, "");
	}
	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof EOF;
	}
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge() && bridgeMatch(target, tol)) {
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
	public Island constructFakeIsland(LexicalNode node, boolean intervalEnd) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
		return new EOF(node, interval);
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertAfter(island, node);
	}
	public LexicalNode clone(LexicalNode previous) {
		return new SOF(getInterval().clone());
	}
}
