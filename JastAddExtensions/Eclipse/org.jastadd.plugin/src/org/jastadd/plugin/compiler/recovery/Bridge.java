package org.jastadd.plugin.compiler.recovery;

public abstract class Bridge {
	protected Island start;
	protected Island end;
	protected boolean visited;

	public Bridge(Island start, Island end) {
		this.start = start;
		this.end = end;
		visited = false;
	}
	
	public void setVisited(boolean value) {
		visited = value;
	}

	public boolean isVisited() {
		return visited;
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
