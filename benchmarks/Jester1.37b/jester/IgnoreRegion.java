package jester;

public class IgnoreRegion {
	private int indexOfEnd, indexOfStart;

	public IgnoreRegion(int indexOfStart, int indexOfEnd) {
		this.indexOfStart = indexOfStart;
		this.indexOfEnd = indexOfEnd;
	}

	public boolean includes(int index) {
		return index >= indexOfStart && index <= indexOfEnd;
	}
	
	public String toString() {
		return "IgnoreRegion [from "+indexOfStart+" to "+indexOfEnd+"]";
	}

	public String within(String source) {
		return source.substring(indexOfStart, indexOfEnd);
	}
}
