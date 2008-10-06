package AST;

import java.util.Deque;
import java.util.LinkedList;

public class DependentList {
    private ASTNode home;
    private Deque<Caching.CacheRoot> deps = new LinkedList<Caching.CacheRoot>();

    public DependentList(ASTNode home) {
	this.home = home;
    }

    public void add(Caching.CacheRoot root) {
      /* ideally, the root should never be null, since every node
	 should have a cache root
	 sometimes, however, we may call lexeme accessors on nodes that
	 are not in the tree yet, which might not have cache roots */
	if(root != null && !deps.contains(root))
	    deps.add(root);
    }

    public void propagate() {
	int sz = deps.size();
	while(sz-- > 0) {
	    deps.poll().propagate();
	}
    }

    public String toString() {
	return deps.toString();
    }
}