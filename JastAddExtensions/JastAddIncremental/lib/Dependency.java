package AST;

public class Dependency {
  protected final AST.ASTNode node;
  protected final int offset;
  protected final java.lang.Object args;

  public Dependency(AST.ASTNode node, int offset) {
    super();
    this.node = node;
    this.offset = offset;
    this.args = null;
  }

  public Dependency(AST.ASTNode node, int offset, java.lang.Object args) {
    super();
    this.node = node;
    this.offset = offset;
    this.args = args;
  }

  public java.lang.Object eval() {
    return node.eval(offset, args);
  }

  public boolean equals(Object o) {
    if(!(o instanceof Dependency))
      return false;
    Dependency d = (Dependency)o;
    if(!node.equals(d.node))
      return false;
    if(offset != d.offset)
       return false;
    if(args == null)
       return d.args == null;
    return args.equals(d.args);
  }

  public int hashCode() {
    return node.hashCode() * offset * (args == null ? 1 : args.hashCode());
  }

  public ASTNode getCacheRoot() {
    return node.getCacheRoot();
  }
}
