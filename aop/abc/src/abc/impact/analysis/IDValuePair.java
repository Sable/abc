package abc.impact.analysis;

import soot.Value;

/**
 * @author Dehua Zhang
 * 
 * Wrap the (int, Value) pair, the integer reprsent the group number,
 * delegate the equals() method to the equivTo() method of Value
 */
final class IDValuePair {
	/**
	 * for example: the index of the param in the args list, starting from 0
	 */
	public final int groupID;

	/**
	 * for example: the variable has the same value of args[index]
	 */
	public final Value value;

	public IDValuePair(final int groupID, final Value reachVariable) {
		this.groupID = groupID;
		this.value = reachVariable;
	}

	public String toString() {
		return "<" + groupID + ", " + value + ">";
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + groupID;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IDValuePair other = (IDValuePair) obj;
		if (groupID != other.groupID)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equivTo(other.value))
			return false;
		return true;
	}
}