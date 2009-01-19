package org.jastadd.plugin.compiler.recovery;

public abstract class Island extends LexicalNode {

	protected Bridge bridge;
	protected boolean fake;

	public Island(LexicalNode previous, Interval interval, String value) {
		super(previous, interval, value);
		fake = false;
	}

	public void setBridge(Bridge bridge) {
		this.bridge = bridge;
	}
	public boolean hasBridge() {
		return bridge != null;
	}
	public Bridge getBridge() {
		return bridge;
	}
	public void setFake(boolean isFake) {
		fake = isFake;
	}
	public boolean isFake() {
		return fake;
	}

	public String toString() {
		return super.toString()  + (isFake()?" (fake)":"");
	}

	public abstract boolean bridgeMatch(Island target, int tol);
	public abstract Bridge buildBridge(Island target, int tol);

	public abstract boolean possibleConstructionSite(LexicalNode node);
	public abstract Island constructFakeIsland(LexicalNode node, boolean intervalEnd);
	public abstract void insertFakeIsland(Island island, LexicalNode node);

	public abstract boolean startOfBridge();
}
