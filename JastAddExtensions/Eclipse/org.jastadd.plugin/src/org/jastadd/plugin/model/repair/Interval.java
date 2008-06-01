package org.jastadd.plugin.model.repair;

public class Interval {
	private int start;
	private int end;

	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
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
	}
}
