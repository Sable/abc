package AST;

public class CachedValue {
  private final DependentList list;
  private int last_computed;
  private Object value;
  private ASTNode home;

  public CachedValue(ASTNode home, Object value) {
    this.list = new DependentList(home);
    this.last_computed = home.getCacheRoot().getLastFlushed();
    this.value = value;
    this.home = home;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    boolean prop = 
      this.value == null ? value != null : !this.value.equals(value);
    this.value = value;
    this.last_computed = home.getCacheRoot().getLastFlushed();
    if(prop)
      propagate();
  }

  public void addDependent(Caching.CacheRoot dep) {
    list.add(dep);
  }

  public void propagate() {
    list.propagate();
  }

  public int last_computed() {
    return last_computed;
  }
}
