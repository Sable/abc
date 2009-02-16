/**
 * 
 */
package abc.impact.utils;

import polyglot.util.Position;

public final class ComparablePosition extends Position implements Comparable {

	private static final long serialVersionUID = 1896284120880922338L;

	public ComparablePosition() {
		super();
	}

	public ComparablePosition(Position arg0, Position arg1) {
		super(arg0, arg1);
	}

	public ComparablePosition(String arg0, int arg1, int arg2, int arg3,
			int arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
	}

	public ComparablePosition(String arg0, int arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public ComparablePosition(String arg0, int arg1) {
		super(arg0, arg1);
	}

	public ComparablePosition(String arg0) {
		super(arg0);
	}

	/**
	 * compare based on filename, line number, column, end line, end column
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {

		Position other = (Position) o;

		return PositionComparator.getInstance().compare(this, other);
	}
}