package test;

/**
 * A memo pair is an entry in a memo line, recording a closure and its expected
 * value.
 */
public class MemoPair {
	
	protected final Dependency dep;
	protected final Object val;
	
	protected MemoPair(Dependency dep, Object val) {
		this.dep = dep;
		this.val = val;
	}
	
	public boolean hit() {
		return dep.eval().equals(val);
	}
	
	public String toString() {
		return "("+dep+"="+val+")";
	}

}
