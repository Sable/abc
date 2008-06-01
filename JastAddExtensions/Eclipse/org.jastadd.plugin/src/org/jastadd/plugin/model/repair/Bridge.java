package org.jastadd.plugin.model.repair;

public abstract class Bridge {
	protected Island start;
	protected Island end;

	public Bridge(Island start, Island end) {
		this.start = start;
		this.end = end;
	}

	public String toString() {
		return start + " >---< " + end;
	}

	public Island getStart() {
		return start;
	}
	public Island getEnd() {
		return end;
	}
}
