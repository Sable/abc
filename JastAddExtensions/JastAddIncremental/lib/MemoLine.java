package AST;

/**
 * A memo line records dependencies.
 */
public class MemoLine {
    private ASTNode node;
    private final java.util.Map<Caching.CacheRoot, Integer> deps = 
	new java.util.HashMap<Caching.CacheRoot, Integer>();
    private boolean checkingHit = false;
    public boolean hit() {
	if(checkingHit)
	    throw new RuntimeException("Circular dependency detected");
	checkingHit = true;
	try {
	    for(java.util.Map.Entry<Caching.CacheRoot, Integer> e 
		    : deps.entrySet())
		if(e.getKey().lastChanged() > e.getValue())
		    return false;
	} finally {
	    checkingHit = false;
	}
	return true;
    }
    public  <T> void add(ASTNode node) {
	Caching.CacheRoot root = node.getCacheRoot();
	if(root == null || deps.containsKey(root))
	    return;
	deps.put(root, root.lastChanged());
    }
    public String toString() {
	return deps.toString();
    }
    public void clear() {
	deps.clear();
    }
    public MemoLine(ASTNode node) {
	super();
	this.node = node;
    }
    public Caching.CacheRoot getCacheRoot() {
	return node.getCacheRoot();
    }
}