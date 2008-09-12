package AST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A memo line records dependencies and their expected values.
 */
public class MemoLine {
    protected final java.util.Map<Dependency, Object> pairs;
    private ASTNode node;
    public MemoLine(ASTNode node) {
	super();
	this.node = node;
	this.pairs = new HashMap<Dependency, Object>();
    }

    /** flag to indicate whether we are checking a cyclic dependency */
    private boolean checkingHit = false;

    public boolean busy() { return checkingHit; }

    public boolean hit() {
	if(checkingHit)
	    return true;
	checkingHit = true;
	try {
	    for(Map.Entry<Dependency, Object> e : pairs.entrySet()) {
		Object v = e.getKey().eval();
		if(v == null) {
		    if(e.getValue() != null) 
			return false;
		} else if(!v.equals(e.getValue()))
		    return false;
	    }
	} finally {
	    checkingHit = false;
	}
	return true;
    }

    public <T> void add(AST.Dependency dep, T val) {
	pairs.put(dep, val);
    }

    public java.lang.String toString() {
	return pairs.toString();
    }

    public void clear() {
	pairs.clear();
    }
}