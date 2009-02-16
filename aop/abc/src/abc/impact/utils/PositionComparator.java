package abc.impact.utils;

import java.util.Comparator;

import polyglot.util.Position;

public final class PositionComparator implements Comparator<Position> {

	private static PositionComparator instance = null;
	
	private PositionComparator() {}
	
	public static PositionComparator getInstance() {
		if (instance == null) {
			instance = new PositionComparator();
		}
		return instance;
	}
	
	/**
	 * Compare two Positions order by filename, line, column, endLine, endColumn
	 * Note: return 0 does not ensure o1.equals(o2) or o2.equals(o1) is ture
	 */
	public int compare(Position o1, Position o2) {
	
		int result = 0;

		// compare the filename
		result = o1.file().compareTo(o2.file());

		// if equal filename, compare the line number
		if (result == 0) {
			int thisLn = o1.line();
			int otherLn = o2.line();
			if (thisLn > otherLn)
				result = 1;
			else if (thisLn == otherLn)
				result = 0;
			else if (thisLn < otherLn)
				result = -1;
		}

		// if equal line number, compare column number
		if (result == 0) {
			int thisCol = o1.column();
			int otherCol = o2.column();
			if (thisCol > otherCol)
				result = 1;
			else if (thisCol == otherCol)
				result = 0;
			else if (thisCol < otherCol)
				result = -1;
		}
		
		// if equal column number, compare end line number
		if (result == 0) {
			int thisLn = o1.endLine();
			int otherLn = o2.endLine();
			if (thisLn > otherLn)
				result = 1;
			else if (thisLn == otherLn)
				result = 0;
			else if (thisLn < otherLn)
				result = -1;
		}
		
		// if equal end line number, compare end column number
		if (result == 0) {
			int thisCol = o1.endColumn();
			int otherCol = o2.endColumn();
			if (thisCol > otherCol)
				result = 1;
			else if (thisCol == otherCol)
				result = 0;
			else if (thisCol < otherCol)
				result = -1;
		}

		return result;
	}
}
