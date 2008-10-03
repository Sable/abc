package AST;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

class AttributeCache {
  private ASTNode home;
  private Map<Object, CachedValue> cache = new HashMap<Object, CachedValue>();

  public AttributeCache(ASTNode home) {
    this.home = home;
  }

  public boolean has(Object o) {
    return cache.containsKey(o);
  }

  public Object get(Object o) {
    return cache.get(o).getValue();
  }

  public void put(Object o, Object val) {
    CachedValue v = cache.get(o);
    if(v == null)
      cache.put(o, new CachedValue(home, val));
    else
      v.setValue(val);
  }

  public void addDependent(Object o, Caching.CacheRoot root) {
    cache.get(o).addDependent(root);
  }

  public int last_computed(Object args) {
    CachedValue v = cache.get(args);
    return v == null ? -1 : v.last_computed();
  }

  public Set<Object> getKeys() {
    return cache.keySet();
  }
}