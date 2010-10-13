package jester;

public class IgnorePair {
	private String start, end;

	public IgnorePair(String start, String end) {
		this.start = start;
		this.end = end;
	}

	public boolean equals(Object other) {
		return other instanceof IgnorePair && equals((IgnorePair)other);
	}
	
	private boolean equals(IgnorePair other) {
		return start.equals(other.start) && end.equals(other.end);
	}

	public int hashCode() {
		return start.hashCode() + end.hashCode();
	}

	public String toString() {
		return "IgnorePair '"+start+"' -> '"+end+"'";
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}

}
