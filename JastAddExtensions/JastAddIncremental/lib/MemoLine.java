package AST;

import java.util.HashMap;
import java.util.Map;

/**
 * A memo line records dependencies and their expected values.
 */
public class MemoLine {
    protected final java.util.Map<Dependency, Object> pairs 
	= new HashMap<Dependency, Object>();
    private boolean checkingHit = false;
    private int last_checked = -1;
    public boolean hit() {
	if(last_checked >= node.state().last_change)
	  return true;
	if(checkingHit)
	    throw new RuntimeException("Circular dependency detected");
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
	last_checked = node.state().last_change;
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
    private ASTNode node;
    public MemoLine(ASTNode node) {
	super();
	this.node = node;
    }
    public ASTNode getCacheRoot() {
	return node.getCacheRoot();
    }
}