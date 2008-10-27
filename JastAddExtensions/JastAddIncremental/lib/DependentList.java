package AST;

import java.util.Deque;
import java.util.LinkedList;

import AST.Caching.CacheRoot;

public class DependentList {
  private AST.ASTNode home;
  private Object deps;
  public DependentList(AST.ASTNode home) {
    super();
    this.home = home;
  }
  public void add(AST.Caching.CacheRoot root) {
    /* ideally, the root should never be null, since every node
       should have a cache root
       sometimes, however, we may call lexeme accessors on nodes that
       are not in the tree yet, which might not have cache roots */
    if(root == null)
      return;
    if(deps == null) {
      deps = root;
    } else if(deps != root) {
      if(deps instanceof CacheRoot) {
	Deque<CacheRoot> d = new LinkedList<CacheRoot>();
	d.add(root);
	d.add((CacheRoot)deps);
	deps = d;
      } else {
	Deque<CacheRoot> d = (Deque<CacheRoot>)deps;
	if(!d.contains(root)) 
	  d.add(root);
      }
    }
  }
  public void propagate() {
    CacheRoot root = home.getCacheRoot();
    if(deps == null || deps == root) {
      deps = null;
      root.propagate();
    } else if(deps instanceof Deque) {
      Deque<CacheRoot> d = (Deque<CacheRoot>)deps;
      if(!d.contains(root)) {
	root.propagate();
	int sz = d.size();
	while(--sz > 0)
	  d.poll().propagate();
      } else {
	int sz = d.size();
	while(sz-- > 0){
	  d.poll().propagate();
	}
      }
    } else {
      CacheRoot r = (CacheRoot)deps;
      deps = null;
      root.propagate();
      r.propagate();
    }
  }
  public java.lang.String toString() {
    return deps.toString();
  }
}
