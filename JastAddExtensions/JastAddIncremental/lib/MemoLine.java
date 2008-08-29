package AST;

import java.util.HashMap;
import java.util.Map;

/**
 * A memo line records dependencies and their expected values.
 */
public class MemoLine {
    protected final java.util.Map<Dependency, Object> pairs 
	= new HashMap<Dependency, Object>();
    public boolean hit() {
	for(Map.Entry<Dependency, Object> e : pairs.entrySet()) {
	    Object v = e.getKey().eval();
	    if(v == null && e.getValue() != null) 
		return false;
	    else if(!v.equals(e.getValue()))
		return false;
	}
	return true;
    }
    public  <T extends java.lang.Object> void add(AST.Dependency dep, T val) {
	pairs.put(dep, val);
    }
    public java.lang.String toString() {
	return pairs.toString();
    }
    public void clear() {
	pairs.clear();
    }
    public MemoLine() {
	super();
    }
}