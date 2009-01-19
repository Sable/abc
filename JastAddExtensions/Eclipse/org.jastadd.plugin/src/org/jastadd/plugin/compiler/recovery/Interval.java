package org.jastadd.plugin.compiler.recovery;

public class Interval {
	private int start;
	private int end;
	private int pushOffset;
	
	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
		pushOffset = 0;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
	
	public int getPushOffset() {
		return pushOffset;
	}

	public static Interval merge(Interval intervalA, Interval intervalB) {
		int start = intervalA.getStart();
		if (intervalB.getStart() < start) {
			start = intervalB.getStart();
		}
		int end = intervalB.getEnd();
		if (intervalA.getEnd() > end) {
			end = intervalA.getEnd();
		}
		return new Interval(start, end);
	}

	public boolean inside(int offset) {
		return start <= offset && offset < end;
	}

	public String toString() {
		return "[" + start + "-" + end + "]";
	}

	public Interval clone() {
		return new Interval(start, end);
	}

	public void pushRight(int offset) {
		start += offset;
		end += offset;
		pushOffset += offset;
	}
}
