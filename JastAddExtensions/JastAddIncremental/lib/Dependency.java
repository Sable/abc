package AST;

public class Dependency {
  protected final AST.ASTNode box;
  protected final int offset;
  protected final java.lang.Object args;

  public Dependency(AST.ASTNode box, int offset) {
    super();
    this.box = box;
    this.offset = offset;
    this.args = null;
  }

  public Dependency(AST.ASTNode box, int offset, java.lang.Object args) {
    super();
    this.box = box;
    this.offset = offset;
    this.args = args;
  }

  public java.lang.Object eval() {
    return box.eval(offset, args);
  }

  public boolean equals(Object o) {
    if(!(o instanceof Dependency))
      return false;
    Dependency d = (Dependency)o;
    if(!box.equals(d.box))
      return false;
    if(offset != d.offset)
       return false;
    if(args == null)
       return d.args == null;
    return args.equals(d.args);
  }

  public int hashCode() {
    return box.hashCode() * offset * (args == null ? 1 : args.hashCode());
  }
}
