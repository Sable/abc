package AST;

class AttributeCache {
  private java.util.Map<java.lang.Object, AST.CachedValue> cache;
  public boolean has(java.lang.Object o) {
    return cache != null && cache.containsKey(o);
  }
  public java.lang.Object get(java.lang.Object o) {
    return cache.get(o).getValue();
  }
  public void put(AST.ASTNode home, java.lang.Object o, java.lang.Object val) {
    if(cache == null) {
      cache = new java.util.HashMap<java.lang.Object, AST.CachedValue>();
      cache.put(o, new AST.CachedValue(home, val));
    }
    else {
      AST.CachedValue v = cache.get(o);
      if(v == null) 
        cache.put(o, new AST.CachedValue(home, val));
      else 
        v.setValue(home, val);
    }
  }
  public void addDependent(java.lang.Object o, AST.Caching.CacheRoot root) {
    cache.get(o).addDependent(root);
  }
  public int last_computed(java.lang.Object args) {
    if(cache == null) 
      return -1;
    AST.CachedValue v = cache.get(args);
    return v == null ? -1 : v.last_computed();
  }
  public java.lang.Object[] getKeys() {
    if(cache == null) 
      return new java.lang.Object[0];
    return cache.keySet().toArray();
  }
}
