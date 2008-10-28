package AST;
import AST.Caching.CacheRoot;

public class DependentList {

  // singleton empty dependent list
  public static final DependentList emptyDependentList =
    new DependentList() {
      @Override public DependentList add(CacheRoot root) {
	return new DependentList().add(root);
      }

      @Override public void propagate(ASTNode home) {
	home.getCacheRoot().propagate();
      }

      @Override	public String toString() {
	return "[]";
      } 
    };

  // prevent direct instantiation
  private DependentList() { 
  }

  // we optimise for the common case that there is exactly one dependency
  // in that case, deps points directly to the depending cache root
  // otherwise it refers to a deque of cache roots
  private java.lang.Object deps;
	
  public DependentList add(AST.Caching.CacheRoot root) {
    if(root == null) 
      return this;
    if(deps == null) {
      deps = root;
    } else if(deps != root) {
      if(deps instanceof AST.Caching.CacheRoot) {
	java.util.Deque<AST.Caching.CacheRoot> d = 
	  new java.util.LinkedList<AST.Caching.CacheRoot>();
	d.add(root);
	d.add((AST.Caching.CacheRoot)deps);
	deps = d;
      } else {
	java.util.Deque<AST.Caching.CacheRoot> d = 
	  (java.util.Deque<AST.Caching.CacheRoot>)deps;
	if(!d.contains(root)) 
	  d.add(root);
      }
    }
    return this;
  }
	
  public void propagate(AST.ASTNode home) {
    AST.Caching.CacheRoot root = home.getCacheRoot();
    if(deps == null || deps == root) {
      deps = null;
      root.propagate();
    } else if(deps instanceof java.util.Deque) {
      java.util.Deque<AST.Caching.CacheRoot> d = 
	(java.util.Deque<AST.Caching.CacheRoot>)deps;
      if(!d.contains(root)) {
	root.propagate();
	int sz = d.size();
	while(--sz > 0)
	  d.poll().propagate();
      } else {
	int sz = d.size();
	while(sz-- > 0)
	  d.poll().propagate();
      }
    } else {
      AST.Caching.CacheRoot r = (AST.Caching.CacheRoot)deps;
      deps = null;
      root.propagate();
      r.propagate();
    }
  }
	
  public java.lang.String toString() {
    return deps.toString();
  }
}
