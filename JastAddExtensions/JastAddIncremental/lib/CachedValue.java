package AST;

public class CachedValue {
  private AST.DependentList list;
  private int last_computed;
  private java.lang.Object value;
  public CachedValue(AST.ASTNode home, java.lang.Object value) {
    super();
    this.list = DependentList.emptyDependentList;
    this.last_computed = home.getCacheRoot().getLastFlushed();
    this.value = value;
  }
  public java.lang.Object getValue() {
    return value;
  }
  public void setValue(AST.ASTNode home, java.lang.Object value) {
    boolean prop = this.value == null ? value != null : !this.value.equals(value);
    this.value = value;
    this.last_computed = home.getCacheRoot().getLastFlushed();
    if(prop) 
      propagate(home);
  }
  public void addDependent(AST.Caching.CacheRoot dep) {
    list = list.add(dep);
  }
  public void propagate(AST.ASTNode home) {
    list.propagate(home);
  }
  public int last_computed() {
    return last_computed;
  }
}
